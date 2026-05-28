package com.gmp.gateway.filter;

import com.gmp.common.constant.SystemConstants;
import com.gmp.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 全局认证过滤器 - 网关层的统一认证鉴权拦截
 *
 * 处理流程：
 * 1. 检查请求是否在白名单中（白名单路径直接放行）
 * 2. 从请求头/ Cookie中提取JWT Token
 * 3. 校验Token是否有效且未过期
 * 4. 检查Token是否在黑名单中（已登出、已强制下线）
 * 5. 解析Token中的用户信息
 * 6. 检查Redis中的活跃会话（单设备登录校验，新登录踢旧登录）
 * 7. 将用户信息通过请求头传递给下游服务并放行
 *
 * 白名单路径（无需认证，但仍需携带有效Token）：
 * - /auth/login, /auth/register (登录注册)
 * - /auth/captcha (验证码)
 * - /auth/refresh-token (Token刷新)
 * - /public/** (公开资源)
 * - /actuator/health (健康检查)
 * - /swagger-ui/**, /v3/api-docs/** (API文档)
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * 白名单路径 - 无需认证即可访问
     */
    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            "/auth/refresh-token",

            "/public/",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/favicon.ico",
            "/flow/",
            "/bpmn/"
    ));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单路径直接放行
        if (isWhiteListed(path)) {
            log.debug("白名单路径放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 从请求头提取Token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("请求缺少Token - 路径: {}", path);
            return unauthorizedResponse(exchange, "未提供认证Token，请先登录");
        }

        // 3. 验证Token有效性
        if (!JwtUtils.validateToken(token)) {
            log.warn("Token无效或已过期 - 路径: {}", path);
            return unauthorizedResponse(exchange, "Token无效或已过期，请重新登录");
        }

        // 4. 检查Token是否在黑名单中（异步Redis查询）
        String blacklistKey = SystemConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(blacklistKey).flatMap(isBlacklisted -> {
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.warn("Token已被加入黑名单 - 路径: {}", path);
                return unauthorizedResponse(exchange, "Token已失效，请重新登录");
            }

            // 5. 解析Token中的用户信息
            try {
                Claims claims = JwtUtils.parseToken(token);
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();

                // 6. 单设备登录校验：检查Redis中的活跃会话
                String sessionKey = SystemConstants.REDIS_SESSION_PREFIX + userId;
                return redisTemplate.opsForValue().get(sessionKey)
                    .flatMap(activeToken -> {
                        if (!activeToken.equals(token)) {
                            log.warn("账号在其他设备登录 - 用户: {} (ID: {}), 当前Token已被踢下线", username, userId);
                            return unauthorizedResponse(exchange, "账号已在其他设备登录，请重新登录");
                        }
                        return proceedWithRequest(exchange, chain, request, claims, path);
                    })
                    .switchIfEmpty(Mono.defer(() ->
                        proceedWithRequest(exchange, chain, request, claims, path)
                    ));

            } catch (Exception e) {
                log.error("解析Token信息失败 - 路径: {}", path, e);
                return unauthorizedResponse(exchange, "Token解析失败");
            }
        });
    }

    /**
     * 构建请求头并放行到下游服务
     */
    private Mono<Void> proceedWithRequest(ServerWebExchange exchange, GatewayFilterChain chain,
                                           ServerHttpRequest request, Claims claims, String path) {
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        String roles = claims.get("roles", String.class);
        String permissions = claims.get("permissions", String.class);
        Long deptId = claims.get("deptId", Long.class);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Name", username)
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-User-Permissions", permissions != null ? permissions : "")
                .header("X-User-Dept-Id", deptId != null ? String.valueOf(deptId) : "")
                .header("X-Request-Path", path)
                .build();

        log.debug("认证通过 - 用户: {} (ID: {}), 路径: {}", username, userId, path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 从请求中提取Token
     * 优先从Authorization请求头提取，其次从Cookie提取
     */
    private String extractToken(ServerHttpRequest request) {
        // 优先从Authorization头获取
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(SystemConstants.TOKEN_PREFIX)) {
            return authHeader.substring(SystemConstants.TOKEN_PREFIX.length());
        }

        // 其次从Cookie获取
        String cookie = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookie != null && cookie.contains(SystemConstants.TOKEN_COOKIE)) {
            // 简化的Cookie解析（生产环境建议使用专门的Cookie解析库）
            String[] parts = cookie.split(";");
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.startsWith(SystemConstants.TOKEN_COOKIE + "=")) {
                    return trimmed.substring(SystemConstants.TOKEN_COOKIE.length() + 1);
                }
            }
        }

        return null;
    }

    /**
     * 返回未授权响应（JSON格式）
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                message, System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 设置过滤器执行顺序（数值越小越先执行）
     * -1: 让此过滤器在所有过滤器之前执行（认证必须先于业务处理）
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
