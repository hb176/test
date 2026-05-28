package com.gmp.system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new GatewayHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }

    /**
     * 从网关注入的请求头中提取用户信息，设置 Spring Security 上下文
     * 网关 AuthGlobalFilter 已完成 JWT 校验，此处仅做信任传递
     */
    static class GatewayHeaderAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String username = request.getHeader("X-User-Name");
            String roles = request.getHeader("X-User-Roles");
            String permissions = request.getHeader("X-User-Permissions");

            if (username != null && !username.isEmpty()) {
                List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                // 角色（如 ROLE_ADMIN）
                if (roles != null && !roles.isEmpty()) {
                    Arrays.stream(roles.split(","))
                            .map(String::trim).filter(r -> !r.isEmpty())
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }
                // 权限标识（如 system:user:add）
                if (permissions != null && !permissions.isEmpty()) {
                    Arrays.stream(permissions.split(","))
                            .map(String::trim).filter(p -> !p.isEmpty())
                            .map(SimpleGrantedAuthority::new)
                            .forEach(authorities::add);
                }

                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        }
    }
}
