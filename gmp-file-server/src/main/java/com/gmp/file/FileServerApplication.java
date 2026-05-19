package com.gmp.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * GMP文件管理服务 - 基于MinIO的高性能对象存储
 *
 * 核心能力（对应DTQ-DMS文件管理）：
 * 1. 文件上传/下载/预览：支持断点续传、分片上传（大文件）
 * 2. 文件版本管理：自动版本递增、历史版本回溯
 * 3. 文件权限控制：基于RBAC的读/写/下载/打印权限
 * 4. GxP合规：21 CFR Part 11电子记录/电子签名支持
 * 5. 文件生命周期：创建→审核→生效→复审→作废（配合流程引擎）
 * 6. 文件元数据：自定义标签、分类、关联业务对象
 * 7. 水印支持：预览/下载时动态添加水印
 *
 * @author hb176
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.gmp.file", "com.gmp.framework", "com.gmp.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.gmp\\.framework\\.workflow\\..*"))
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.gmp.file.mapper")
public class FileServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileServerApplication.class, args);
        System.out.println("GMP File-Server 启动成功 - MinIO文件管理服务 (GxP DMS)");
    }
}
