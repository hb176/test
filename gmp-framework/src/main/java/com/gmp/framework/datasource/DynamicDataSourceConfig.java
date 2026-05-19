package com.gmp.framework.datasource;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 动态多数据源配置 - 基于Baomidou Dynamic Datasource 4.x
 *
 * 说明：
 * Baomidou DynamicDatasource 在 Spring Boot 3.x 的 4.x 版本中
 * 由 DynamicDataSourceAutoConfiguration 自动装配 DynamicRoutingDataSource，
 * 只需在 yml 中配置 spring.datasource.dynamic.primary 和 spring.datasource.dynamic.datasource 即可。
 *
 * 本配置只定义主数据源 Bean，其余由自动配置处理。
 * 如需运行时代码中添加数据源，注入 DynamicRoutingDataSource 直接调用 addDataSource()。
 *
 * 使用方式：
 * @DS("master")    // 使用主库
 * @DS("slave")     // 使用从库（需在yml中配置）
 * @DS("tenant_001") // 多租户数据源（运行时动态添加）
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class DynamicDataSourceConfig {

    /**
     * 主数据源 - 由Druid连接池管理
     * Baomidou自动配置会将其注册为 primary 数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource dataSource() {
        return DruidDataSourceBuilder.create().build();
    }
}
