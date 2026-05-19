package com.gmp.form;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * GMP表单管理服务 - 基于JSON Schema的可配置表单引擎
 *
 * 核心能力：
 * 1. 表单定义管理：表单Schema的CRUD、版本管理、发布/归档
 * 2. 字段配置：支持20+种字段类型、校验规则、联动规则
 * 3. 表单数据CRUD：JSON格式存储、索引字段提取、条件查询
 * 4. 子表单支持：嵌套表格、动态行
 * 5. 表单模板：预设模板快速创建
 * 6. 权限绑定：表单与角色/流程绑定
 *
 * @author hb176
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.gmp.form", "com.gmp.framework", "com.gmp.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.gmp\\.framework\\.workflow\\..*"))
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.gmp.form.mapper")
public class FormServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(FormServerApplication.class, args);
        System.out.println("GMP Form-Server 启动成功 - 可配置表单服务 (JSON Schema引擎)");
    }
}
