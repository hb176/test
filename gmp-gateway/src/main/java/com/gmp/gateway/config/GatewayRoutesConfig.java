package com.gmp.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * 网关路由配置 - 定义请求路径到微服务的映射关系
 *
 * 路由规则说明：
 * /auth/**      -> gmp-auth (认证授权服务)
 * /system/**    -> gmp-system-server (系统管理服务)
 * /form/**      -> gmp-form-server (表单管理服务)
 * /workflow/**  -> gmp-workflow-server (流程管理服务)
 * /file/**      -> gmp-file-server (文件服务，预留)
 * /message/**   -> gmp-message-server (消息服务，预留)
 *
 * 高级特性：
 * 1. 服务名自动通过Nacos解析为实际的IP:Port
 * 2. 内置负载均衡（通过Spring Cloud LoadBalancer）
 * 3. 支持按用户/IP限流（通过Redis Rate Limiter）
 * 4. 支持请求重试和熔断（通过Resilience4J，可选）
 *
 * @author hb176
 * @since 1.0.0
 */
@Configuration
public class GatewayRoutesConfig {

    /**
     * 定义网关路由规则
     * 使用Java DSL方式配置，也可通过spring.cloud.gateway.routes配置
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route("gmp-auth", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)             // 不剥离前缀
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-auth")              // lb:// 表示使用负载均衡
                )
                // 系统管理服务路由
                .route("gmp-system-server", r -> r
                        .path("/system/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-system-server")
                )
                // 表单服务路由
                .route("gmp-form-server", r -> r
                        .path("/form/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-form-server")
                )
                // 流程服务路由 (含BPMN设计器API)
                .route("gmp-workflow-server", r -> r
                        .path("/workflow/**", "/flow/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-workflow-server")
                )
                // 业务服务路由 (QMS/DMS/TMS/QRS)
                .route("gmp-business-server", r -> r
                        .path("/qms/**", "/dms/**", "/tms/**", "/qrs/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-business-server")
                )
                // 文件服务路由
                .route("gmp-file-server", r -> r
                        .path("/file/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Route-Source", "gateway")
                        )
                        .uri("lb://gmp-file-server")
                )
                // 公开资源路由
                .route("public-resources", r -> r
                        .path("/public/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("no://op")
                )
                .build();
    }

    /**
     * 限流键解析器 - 基于请求IP进行限流
     * 也可改为基于用户ID的限流（KeyResolver based on user）
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            return Mono.just(ip);
        };
    }

    /**
     * Redis限流器配置
     * replenishRate: 每秒允许的请求数（令牌桶填充速率）
     * burstCapacity: 突发容量（令牌桶最大容量）
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        // 默认: 每秒100个请求，突发容量200
        return new RedisRateLimiter(100, 200);
    }
}
