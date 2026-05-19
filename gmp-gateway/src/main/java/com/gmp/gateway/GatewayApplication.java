package com.gmp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * GMP API网关启动类 - 基于Spring Cloud Gateway (Reactive)
 *
 * 核心职责：
 * 1. 统一入口：所有外部请求通过网关进入系统
 * 2. 路由转发：根据请求路径动态路由到对应微服务
 * 3. 认证鉴权：解析JWT Token并校验权限
 * 4. 限流熔断：保护后端服务不被突发流量压垮
 * 5. 跨域处理：统一处理CORS跨域请求
 * 6. 请求日志：记录所有API调用日志
 *
 * 技术选型说明：
 * - 使用Spring Cloud Gateway（非Zuul），基于Reactive/Netty，性能更高
 * - 使用Nacos作为服务发现，支持动态路由更新
 * - 使用Redis Reactive进行限流计数
 *
 * @author hb176
 * @since 1.0.0
 */
// 👇 关键：排除数据库自动配置

// 👇 新增排除 Security，彻底解决安全拦截
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
// 启用Nacos服务发现（自动从Nacos获取服务列表）
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  GMP API网关启动成功！");
        System.out.println("  技术栈: Spring Cloud Gateway + Nacos + Netty");
        System.out.println("  功能: 路由转发 | 认证鉴权 | 限流 | 跨域 | 日志");
        System.out.println("==========================================================");
    }
}
