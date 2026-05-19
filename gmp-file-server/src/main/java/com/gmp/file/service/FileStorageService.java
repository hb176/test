package com.gmp.file.service;

import cn.hutool.core.io.FileUtil;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.common.base.ResultCode;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件存储服务 - MinIO对象存储操作封装
 *
 * 核心操作：
 * 1. 文件上传（普通/分片）
 * 2. 文件下载（流式返回）
 * 3. 文件预览URL生成（临时签名URL）
 * 4. 文件删除
 * 5. 存储桶管理
 *
 * 安全策略：
 * - 所有文件访问通过签名URL（默认7天有效）
 * - 不直接暴露MinIO端点
 * - 上传文件进行类型检测（防止恶意文件）
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    /** 允许上传的文件类型（白名单） */
    private static final String[] ALLOWED_EXTENSIONS = {
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "rtf",
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "svg",
            "zip", "rar", "7z",
            "mp4", "avi", "mov",
            "xml", "json"
    };

    /** 禁止上传的文件类型（黑名单，安全考虑） */
    private static final String[] BLOCKED_EXTENSIONS = {
            "exe", "bat", "sh", "cmd", "ps1", "vbs", "jar", "war",
            "php", "jsp", "asp", "aspx"
    };

    /**
     * 上传文件到MinIO
     *
     * @param file       上传的文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（为空则自动生成UUID名称）
     * @return 上传结果（包含bucketName和objectName）
     */
    public UploadResult uploadFile(MultipartFile file, String bucketName, String objectName) {
        // 1. 文件类型安全检测
        validateFileType(file);

        try {
            // 2. 确保存储桶存在
            ensureBucketExists(bucketName);

            // 3. 生成对象名称（保留原始扩展名）
            if (objectName == null || objectName.isBlank()) {
                String ext = FileUtil.extName(file.getOriginalFilename());
                objectName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
            }

            // 4. 上传文件流
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            log.info("文件上传成功 - bucket: {}, object: {}, size: {} bytes, type: {}",
                    bucketName, objectName, file.getSize(), file.getContentType());

            return new UploadResult(bucketName, objectName, file.getSize(), file.getContentType());

        } catch (Exception e) {
            log.error("文件上传失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件（返回文件流）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_ERROR, "文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预览URL（临时签名URL）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expireSeconds URL有效时间（秒），默认7天
     * @return 临时签名URL
     */
    public String getPresignedUrl(String bucketName, String objectName, int expireSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            log.error("生成预览URL失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_ERROR, "生成预览链接失败");
        }
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("文件删除成功 - bucket: {}, object: {}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件删除失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_ERROR, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件元数据信息
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件状态信息
     */
    public StatObjectResponse getFileStat(String bucketName, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("获取文件元数据失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new BusinessException(ResultCode.FILE_ERROR, "文件不存在或无法访问");
        }
    }

    /**
     * 确保存储桶存在，不存在则创建
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("创建MinIO存储桶: {}", bucketName);
        }
    }

    /**
     * 验证文件类型安全性
     */
    private void validateFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不能为空");
        }

        String ext = FileUtil.extName(originalFilename).toLowerCase();

        // 黑名单检测
        for (String blocked : BLOCKED_EXTENSIONS) {
            if (blocked.equals(ext)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "不允许上传该类型文件: " + ext);
            }
        }

        // 白名单检测
        boolean allowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(ext)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            log.warn("上传文件类型不在白名单中: {}", ext);
            // 可根据业务需要决定是否严格限制
        }
    }

    /**
     * 文件上传结果
     */
    public record UploadResult(String bucketName, String objectName, long fileSize, String contentType) {}
}
