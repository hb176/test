package com.gmp.file.config;


import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**连接地址*/
    private String endpoint;

    /**用户名*/
    private String accessKey;

    /**密码*/
    private String secretKey;

    /**域名*/
    //private String filHost;

    /**临时桶目录*/
    private String tempBucket;

    /**DMS草稿文件子桶名称*/
    private String draftBucket;

    /**DMS正式文件子桶名称*/
    private String officialBucket;

    /**TMS骄教材附件子桶名称*/
    private String trainBookBucket;

    /**TMS题库附件子桶名称*/
    private String questionBucket;

    /**所有系统附件子桶名称*/
    private String accessoryBucket;

    /**所有系统common桶名称*/
    private String commonBucket;

    /**所有系统abioplus桶名称*/
    private String abioplusBucket;

    /**QMS桶名称*/
    private String qmsBucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}