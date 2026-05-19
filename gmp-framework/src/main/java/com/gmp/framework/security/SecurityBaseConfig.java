package com.gmp.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全基础配置 - Spring Security核心组件配置
 *
 * 提供：
 * 1. BCrypt密码编码器 - 用于密码加密存储和验证
 * 2. BCrypt特点：自动加盐、不可逆、计算强度可调(默认10轮)
 *
 * 注意：具体的认证过滤器、授权规则由各微服务模块自行配置
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class SecurityBaseConfig {

    /**
     * BCrypt密码编码器
     * BCrypt是目前业界公认安全的密码哈希算法之一
     * 每次加密结果不同（自动加盐），有效防止彩虹表攻击
     *
     * 使用方式：
     * - 加密: passwordEncoder.encode(rawPassword)
     * - 验证: passwordEncoder.matches(rawPassword, encodedPassword)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
