package com.gmp.file.utils;

import com.gmp.file.config.MinioConfig;
import com.gmp.common.base.BasicConstant;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


/**
 * @author hb176
 * @time 2021-3-20 10:59
 * @email yuanfeng@abioplus.cn
 * @Description: minio工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinIoUtil {

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");

    private final MinioConfig minIoConfig;
    private final MinioClient minioClient;

    /**
     * 创建bucket
     *
     * @param bucketName
     * @throws Exception
     */
    public void createBucket(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                } catch (ErrorResponseException e) {
                    e.printStackTrace();
                } catch (InsufficientDataException e) {
                    e.printStackTrace();
                } catch (InternalException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidResponseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (ServerException e) {
                    e.printStackTrace();
                } catch (XmlParserException e) {
                    e.printStackTrace();
                }
            }
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (XmlParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件到minIo服务器
     *
     * @param file
     * @param bucketName
     * @return
     * @throws Exception
     */
    public String uploadFile(MultipartFile file, String bucketName, String childBucket, String fileName) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            return null;
        }
        //判断桶名称是否为空
        if (StringUtils.isEmpty(bucketName)) {
            return null;
        }
        //判断存储桶是否存在  不存在则创建
        createBucket(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 存储桶文件名_时间戳.后缀名
//        String uuid = UUID.randomUUID().toString().replaceAll(BasicConstant.SPLIT_REGEX02, "").substring(0, 6);
        String fName = dateFormat.format(new Date());
        assert originalFilename != null;

        String storagePath = "";
        if (StringUtils.isBlank(childBucket)) {
            storagePath = (StringUtils.isNotBlank(fileName) ? fileName : fName) + originalFilename.substring(originalFilename.lastIndexOf(BasicConstant.SPLIT_REGEX07));
        } else {
            storagePath = childBucket.toLowerCase() + BasicConstant.SPLIT_REGEX06 + (StringUtils.isNotBlank(fileName) ? fileName : fName) + originalFilename.substring(originalFilename.lastIndexOf(BasicConstant.SPLIT_REGEX07));
        }
        //开始上传

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(storagePath)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

//        File toFile = FileUtils.multipartFileToFile(file);
//        FileUtils.delteTempFile(toFile);
        return storagePath;
    }

    /**
     * 获取全部bucket
     *
     * @return
     */
    public List<Bucket> getAllBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    /**
     * 获取文件的大小
     * @param bucketName
     * @param objectName
     * @return
     * @throws Exception
     */
    public Long getFileSize(String bucketName, String objectName) throws Exception {
        // 获取文件的大小
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        ).size();


    }


    /**
     * 根据bucketName获取信息
     *
     * @param bucketName bucket名称
     */
    public Optional<Bucket> getBucket(String bucketName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InsufficientDataException, InvalidResponseException, InternalException, ErrorResponseException, ServerException, XmlParserException {
        return minioClient.listBuckets().stream().filter(b -> b.name().equals(bucketName)).findFirst();
    }

    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 获取⽂件外链
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param expires    过期时间 <=7
     * @return url
     */
    public String getObjectURLWithExpires(String bucketName, String objectName, Integer expires) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires)
                        .build()
        );
    }

    /**
     * 获取⽂件外链
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @return url
     */
    public String getObjectURL(String bucketName, String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 获取⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @return ⼆进制流
     */
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 上传⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @param stream     ⽂件流
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream) throws
            Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(stream, stream.available(), -1)
                .contentType(objectName.substring(objectName.lastIndexOf(".")))
                .build());
    }

    /**
     * 上传⽂件
     *
     * @param bucketName  bucket名称
     * @param objectName  ⽂件名称
     * @param stream      ⽂件流
     * @param size        ⼤⼩
     * @param contextType 类型
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#putObject
     */
    public void putObject(String bucketName, String objectName, InputStream stream, long
            size, String contextType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName).object(objectName)
                .stream(stream, size, -1)
                .contentType(contextType)
                .build());
    }

    /**
     * @param bucketName    bucket名称
     * @param objectName    文件名称
     * @param srcBucketName 目标bucket名称
     * @param srcObjectName 目标文件名称
     * @throws Exception
     */
    public String copyObject(String bucketName, String objectName, String srcBucketName, String srcObjectName) throws Exception {
        minioClient.copyObject(CopyObjectArgs.builder()
                .source(CopySource.builder().bucket(bucketName).object(objectName).build())
                .bucket(srcBucketName)
                .object(srcObjectName)
                .build());
        return srcObjectName;
    }

    /**
     * 获取⽂件信息
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#statObject
     */
    public StatObjectResponse getObjectInfo(String bucketName, String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 删除⽂件
     *
     * @param bucketName bucket名称
     * @param objectName ⽂件名称
     * @throws Exception https://docs.minio.io/cn/java-client-apireference.html#removeObject
     */
    public void removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * 删除多个⽂件
     *
     * @param bucketName       bucket名称
     * @param deleteObjectList 多个文件路径
     * @throws Exception https://docs.minio.io/cn/java-client-apireference.html#removeObjects
     */
    public void removeObjects(String bucketName, List<String> deleteObjectList) throws Exception {
        Stream<DeleteObject> stream = deleteObjectList.stream().map(DeleteObject::new);
        minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(stream::iterator).build());
    }

    /**
     * 获取长效的文件url
     *
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     * @throws Exception
     */
    public String presignedGetObject(String bucketName, String objectName, Integer expires) throws Exception {
        BucketExistsArgs bucketArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        boolean bucketExists = minioClient.bucketExists(bucketArgs);
        String url = "";
        if (bucketExists) {
            if (expires == null) {
                expires = 604800;
            }
            GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expires)
                    .build();
            url = minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
            log.info("url:" + url);
        }
        return url;
    }

}