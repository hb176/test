package com.gmp.common.utils;

import com.gmp.common.constant.SystemConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类 - Token的生成、解析、验证
 * 使用HMAC-SHA256算法对Token进行签名，确保Token的完整性和不可篡改性
 *
 * 注意：生产环境中密钥应从配置中心(Nacos)或环境变量中读取，不能硬编码
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
public class JwtUtils {

    /**
     * JWT签名密钥 (HMAC-SHA256算法要求至少256位=32字节)
     * 生产环境必须通过配置中心动态获取
     */
    private static final String SECRET = "GMP-System-JWT-SecretKey-For-HMAC-SHA256-Must-Be-At-Least-256Bits!!";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * JWT签发者标识
     */
    private static final String ISSUER = "GMP-System";

    // ==================== Token生成 ====================

    /**
     * 生成访问Token (Access Token)，使用系统默认过期时间
     */
    public static String createAccessToken(Long userId, String username, String roles) {
        return createAccessToken(userId, username, roles, SystemConstants.TOKEN_EXPIRE_SECONDS);
    }

    /**
     * 生成访问Token (Access Token)，指定过期时间
     *
     * @param userId        用户ID
     * @param username      用户名
     * @param roles         用户角色列表（逗号分隔）
     * @param expireSeconds Token有效秒数
     * @return JWT Token字符串
     */
    public static String createAccessToken(Long userId, String username, String roles, long expireSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", "access");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .id(String.valueOf(userId))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 生成刷新Token (Refresh Token) - 有效期更长
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Refresh Token字符串
     */
    public static String createRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + SystemConstants.REFRESH_TOKEN_EXPIRE_SECONDS * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuer(ISSUER)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .id(String.valueOf(userId))
                .signWith(SECRET_KEY)
                .compact();
    }

    // ==================== Token解析 ====================

    /**
     * 解析Token并返回Claims
     *
     * @param token JWT Token字符串
     * @return JWT Claims (包含payload中的所有声明)
     * @throws JwtException Token无效或已过期
     */
    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID，解析失败返回null
     */
    public static Long getUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (JwtException e) {
            log.warn("从Token提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名，解析失败返回null
     */
    public static String getUsername(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            log.warn("从Token提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取用户角色
     *
     * @param token JWT Token字符串
     * @return 角色字符串（逗号分隔），解析失败返回null
     */
    public static String getRoles(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("roles", String.class);
        } catch (JwtException e) {
            log.warn("从Token提取角色失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Token验证 ====================

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @return true=有效, false=无效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否即将过期（在指定时间内过期）
     *
     * @param token         JWT Token字符串
     * @param withinSeconds 检查的秒数范围
     * @return true=即将过期
     */
    public static boolean isTokenExpiringSoon(String token, long withinSeconds) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime > 0 && remainingTime < withinSeconds * 1000;
        } catch (JwtException e) {
            return true; // 已过期或无效
        }
    }
}
