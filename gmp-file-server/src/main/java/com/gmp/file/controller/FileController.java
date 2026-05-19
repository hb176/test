package com.gmp.file.controller;

import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.file.service.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 文件管理控制器 - 文件上传/下载/预览/删除/元数据
 *
 * API设计：
 * POST   /file/upload          - 上传文件
 * GET    /file/download/{id}   - 下载文件
 * GET    /file/preview/{id}    - 获取预览URL
 * DELETE /file/{id}            - 删除文件
 * GET    /file/{id}/info       - 获取文件元数据
 * GET    /file/{id}/versions   - 获取文件版本历史
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传文件
     * POST /file/upload
     * 参数: file (MultipartFile), bucketName (可选), businessType (可选), businessId (可选)
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String bucketName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId) {

        if (file.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        // 使用默认bucket
        if (bucketName == null || bucketName.isBlank()) {
            bucketName = "gmp-files";
        }

        // 调用存储服务上传
        FileStorageService.UploadResult uploadResult = fileStorageService.uploadFile(file, bucketName, null);

        // 构建响应（实际项目中还需保存file_info记录到数据库）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fileName", file.getOriginalFilename());
        result.put("fileSize", uploadResult.fileSize());
        result.put("mimeType", uploadResult.contentType());
        result.put("bucketName", uploadResult.bucketName());
        result.put("objectName", uploadResult.objectName());
        result.put("uploadTime", new Date());

        log.info("文件上传成功 - 原始文件名: {}, 存储路径: {}/{}",
                file.getOriginalFilename(), uploadResult.bucketName(), uploadResult.objectName());

        return Result.ok("上传成功", result);
    }

    /**
     * 批量上传文件
     * POST /file/upload-batch
     */
    @PostMapping("/upload-batch")
    public Result<List<Map<String, Object>>> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(required = false) String bucketName) {

        if (bucketName == null || bucketName.isBlank()) {
            bucketName = "gmp-files";
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                FileStorageService.UploadResult r = fileStorageService.uploadFile(file, bucketName, null);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("fileName", file.getOriginalFilename());
                item.put("objectName", r.objectName());
                results.add(item);
            }
        }

        return Result.ok("批量上传完成，成功: " + results.size(), results);
    }

    /**
     * 下载文件
     * GET /file/download/{bucketName}/{objectName}
     */
    @GetMapping("/download/{bucketName}/{objectName}")
    public void downloadFile(@PathVariable String bucketName,
                              @PathVariable String objectName,
                              @RequestParam(required = false) String fileName,
                              HttpServletResponse response) {
        try {
            InputStream inputStream = fileStorageService.downloadFile(bucketName, objectName);

            // 设置响应头
            String downloadName = fileName != null ? fileName : objectName;
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(downloadName, StandardCharsets.UTF_8) + "\"");

            // 流式写入响应
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

        } catch (Exception e) {
            log.error("文件下载失败 - bucket: {}, object: {}", bucketName, objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 获取文件预览URL（临时签名URL，默认7天有效）
     * GET /file/preview/{bucketName}/{objectName}?expireSeconds=604800
     */
    @GetMapping("/preview/{bucketName}/{objectName}")
    public Result<Map<String, Object>> getPreviewUrl(
            @PathVariable String bucketName,
            @PathVariable String objectName,
            @RequestParam(defaultValue = "604800") int expireSeconds) {

        String presignedUrl = fileStorageService.getPresignedUrl(bucketName, objectName, expireSeconds);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", presignedUrl);
        result.put("expireSeconds", expireSeconds);
        result.put("expireTime", new Date(System.currentTimeMillis() + expireSeconds * 1000L));

        return Result.ok(result);
    }

    /**
     * 删除文件
     * DELETE /file/{bucketName}/{objectName}
     */
    @DeleteMapping("/{bucketName}/{objectName}")
    public Result<Void> deleteFile(@PathVariable String bucketName,
                                    @PathVariable String objectName) {
        fileStorageService.deleteFile(bucketName, objectName);
        log.info("文件已删除 - bucket: {}, object: {}", bucketName, objectName);
        return Result.okMsg("删除成功");
    }

    /**
     * 获取文件元数据（大小、类型、修改时间等）
     */
    @GetMapping("/stat/{bucketName}/{objectName}")
    public Result<Map<String, Object>> getFileStat(@PathVariable String bucketName,
                                                    @PathVariable String objectName) {
        var stat = fileStorageService.getFileStat(bucketName, objectName);
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("objectName", stat.object());
        info.put("size", stat.size());
        info.put("contentType", stat.contentType());
        info.put("lastModified", stat.lastModified());
        info.put("etag", stat.etag());
        return Result.ok(info);
    }
}
