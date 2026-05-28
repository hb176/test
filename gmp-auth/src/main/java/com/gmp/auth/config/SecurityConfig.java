package com.gmp.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security安全配置 - 认证授权规则定义
 *
 * 配置策略：
 * 1. 无状态会话（STATELESS）- 不使用HttpSession，每次请求携带JWT
 * 2. CSRF禁用 - REST API不需要CSRF保护
 * 3. 基于注解的权限控制 - 使用@PreAuthorize在方法级控制权限
 * 4. BCrypt密码加密 - 安全存储用户密码
 *
 * 白名单路径（无需认证）：
 * - /auth/login, /auth/register, /auth/captcha (认证相关)
 * - /public/** (公开资源)
 * - /actuator/health (健康检查)
 * - /swagger-ui/**, /v3/api-docs/** (API文档)
 *
 * @author hb176
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
// 启用方法级权限控制（支持@PreAuthorize, @Secured, @RolesAllowed）
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    /**
     * 安全过滤器链配置
     * 定义URL访问规则、会话策略、JWT过滤器集成
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF保护禁用（REST API基于Token，不需要CSRF）
                .csrf(csrf -> csrf.disable())

                // 2. 无状态会话（每次请求都携带JWT，不依赖服务端Session）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. URL访问规则
                .authorizeHttpRequests(auth -> auth
                        // 白名单路径：无需认证
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/captcha",
                                "/auth/refresh-token",
                                "/auth/token-expire-config",
                                "/auth/user-info",
                                "/auth/user-menus",
                                "/auth/logout",
                                "/public/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 系统管理接口：需要ADMIN角色
                        .requestMatchers("/system/admin/**").hasRole("ADMIN")
                        // 其他所有请求：需要认证
                        .anyRequest().authenticated()
                )

                // 4. 添加自定义JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前）
                // .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 5. 表单登录禁用（使用JWT自定义登录接口）
                .formLogin(form -> form.disable())

                // 6. HTTP Basic认证禁用
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
