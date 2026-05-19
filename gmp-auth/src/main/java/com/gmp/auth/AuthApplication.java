package com.gmp.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * GMP认证授权服务启动类
 *
 * 核心职责：
 * 1. 用户登录认证（用户名/密码 + 验证码）
 * 2. JWT Token签发与刷新
 * 3. Token黑名单管理（登出/强制下线）
 * 4. RBAC权限管理（角色-菜单-权限）
 * 5. OAuth2.1资源服务器（保护API资源）
 *
 * 安全架构：
 * - 认证：Spring Security + JWT (RSA签名)
 * - 存储：BCrypt密码加密 + Redis Token管理
 * - 授权：RBAC模型（用户 -> 角色 -> 权限）
 * - 验证码：Redis存储 + 图形验证码
 *
 * @author hb176
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.gmp.auth", "com.gmp.framework", "com.gmp.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.gmp\\.framework\\.workflow\\..*"))
@EnableDiscoveryClient
@MapperScan("com.gmp.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  GMP 认证授权服务启动成功！");
        System.out.println("  技术栈: Spring Security + JWT + OAuth2.1 + Redis");
        System.out.println("  功能: 登录认证 | Token管理 | RBAC授权 | 验证码");
        System.out.println("============================ ==============================");
    }
}
