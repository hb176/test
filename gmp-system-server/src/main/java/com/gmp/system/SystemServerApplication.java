package com.gmp.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * GMP系统管理服务 - 用户/角色/菜单/部门/字典/配置/日志
 *
 * @author hb176
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.gmp.system", "com.gmp.framework", "com.gmp.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.gmp\\.framework\\.workflow\\..*"))
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.gmp.system.mapper")
public class SystemServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemServerApplication.class, args);
        System.out.println("GMP System-Server 启动成功 - 系统管理服务");
    }
}
