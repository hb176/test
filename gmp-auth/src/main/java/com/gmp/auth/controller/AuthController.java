package com.gmp.auth.controller;

import com.gmp.auth.entity.SysOperLog;
import com.gmp.auth.mapper.SysOperLogMapper;
import com.gmp.auth.service.AuthUserDetailsService;
import com.gmp.auth.service.TokenConfigService;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.common.constant.SystemConstants;
import com.gmp.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfigService tokenConfigService;
    private final SysOperLogMapper operLogMapper;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        long start = System.currentTimeMillis();
        log.info("用户登录请求 - username: {}", req.getUsername());

        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return Result.fail(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.fail(ResultCode.BAD_REQUEST, "密码不能为空");
        }

        // 验证码校验
        if (req.getCaptchaKey() != null && !req.getCaptchaKey().isBlank()) {
            String captchaRedisKey = SystemConstants.REDIS_CAPTCHA_PREFIX + req.getCaptchaKey();
            String storedCode = redisTemplate.opsForValue().get(captchaRedisKey);
            if (storedCode == null) {
                return Result.fail(ResultCode.BAD_REQUEST, "验证码已过期，请刷新后重试");
            }
            if (!storedCode.equalsIgnoreCase(req.getCaptchaCode())) {
                return Result.fail(ResultCode.BAD_REQUEST, "验证码错误");
            }
            redisTemplate.delete(captchaRedisKey);
        }

        try {
            UserDetails user = userDetailsService.loadUserByUsername(req.getUsername());
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                saveLoginLog(req.getUsername(), "FAIL", "密码错误", start);
                return Result.fail(ResultCode.UNAUTHORIZED, "密码错误");
            }
        } catch (Exception e) {
            log.warn("登录失败: {}", e.getMessage());
            saveLoginLog(req.getUsername(), "FAIL", "用户名或密码错误", start);
            return Result.fail(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        Long id = userDetailsService.getIdByUsername(req.getUsername());
        Long deptId = userDetailsService.getDeptIdByUsername(req.getUsername());
        String roles = userDetailsService.getRolesByUsername(req.getUsername());
        String permissions = userDetailsService.getPermissionsByUsername(req.getUsername());
        long expireSeconds = tokenConfigService.getExpireSeconds();
        String accessToken = JwtUtils.createAccessToken(id, req.getUsername(), roles, deptId, permissions, expireSeconds);
        String refreshToken = JwtUtils.createRefreshToken(id, req.getUsername());

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("accessToken", accessToken);
        resultData.put("refreshToken", refreshToken);
        resultData.put("expiresIn", expireSeconds);
        resultData.put("tokenType", "Bearer");

        // 单设备登录：将当前token存入Redis，替换旧会话
        String sessionKey = SystemConstants.REDIS_SESSION_PREFIX + id;
        redisTemplate.opsForValue().set(sessionKey, accessToken, expireSeconds, TimeUnit.SECONDS);

        log.info("用户登录成功 - username: {}, id: {}", req.getUsername(), id);
        saveLoginLog(req.getUsername(), "SUCCESS", null, start);
        return Result.ok("登录成功", resultData);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(SystemConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SystemConstants.TOKEN_PREFIX.length());
            try {
                Claims claims = JwtUtils.parseToken(token);
                long ttlMs = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (ttlMs > 0) {
                    String blacklistKey = SystemConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token;
                    redisTemplate.opsForValue().set(blacklistKey, "1", ttlMs, TimeUnit.MILLISECONDS);
                    log.info("用户登出 - Token已加入黑名单, 用户: {}", claims.getSubject());
                }
                // 清除活跃会话记录（单设备登录）
                Long userId = claims.get("userId", Long.class);
                if (userId != null) {
                    redisTemplate.delete(SystemConstants.REDIS_SESSION_PREFIX + userId);
                    log.info("用户登出 - 已清除活跃会话, userId: {}", userId);
                }
            } catch (Exception e) {
                log.warn("登出时Token解析失败: {}", e.getMessage());
            }
        }
        return Result.okMsg("登出成功");
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (!JwtUtils.validateToken(request.getRefreshToken())) {
            return Result.fail(ResultCode.UNAUTHORIZED, "Refresh Token无效或已过期");
        }

        Long userId = JwtUtils.getUserId(request.getRefreshToken());
        String username = JwtUtils.getUsername(request.getRefreshToken());

        // 单设备登录校验：检查旧access token是否匹配当前活跃会话
        if (request.getOldAccessToken() != null) {
            String sessionKey = SystemConstants.REDIS_SESSION_PREFIX + userId;
            String activeToken = redisTemplate.opsForValue().get(sessionKey);
            if (activeToken != null && !activeToken.equals(request.getOldAccessToken())) {
                log.warn("Token刷新被拒绝 - 用户已在其他设备登录, 用户: {}", username);
                return Result.fail(ResultCode.UNAUTHORIZED, "账号已在其他设备登录，请重新登录");
            }
        }

        String roles = JwtUtils.getRoles(request.getRefreshToken());
        Long deptId = userDetailsService.getDeptIdByUsername(username);
        String newAccessToken = JwtUtils.createAccessToken(userId, username, roles, deptId, tokenConfigService.getExpireSeconds());

        // 刷新token后更新活跃会话
        String sessionKey = SystemConstants.REDIS_SESSION_PREFIX + userId;
        redisTemplate.opsForValue().set(sessionKey, newAccessToken,
                tokenConfigService.getExpireSeconds(), TimeUnit.SECONDS);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("accessToken", newAccessToken);
        resultData.put("expiresIn", tokenConfigService.getExpireSeconds());

        log.info("Token刷新成功 - 用户: {}", username);
        return Result.ok(resultData);
    }

    /**
     * 获取验证码 — 生成随机4位字母数字码，返回key供登录时校验
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String code = generateCaptchaCode(4);
        String redisKey = SystemConstants.REDIS_CAPTCHA_PREFIX + key;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaCode", code); // 生产环境应改为返回图片，此处简化返回文本便于调试
        return Result.ok(result);
    }

    /**
     * 二次认证（电子签名前置） — 验证密码后返回短期凭证
     * 用于关键操作（审批、文件生效、配置变更）前的身份再确认
     */
    @PostMapping("/re-authenticate")
    public Result<Map<String, String>> reAuthenticate(
            @RequestHeader(value = "X-User-Id", required = false) Long xUserId,
            @RequestHeader(value = "X-User-Name", required = false) String xUsername,
            @RequestBody ReAuthRequest req) {
        String username = xUsername;
        if (username == null) {
            return Result.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return Result.fail(ResultCode.UNAUTHORIZED, "密码错误");
            }
        } catch (Exception e) {
            return Result.fail(ResultCode.UNAUTHORIZED, "身份验证失败");
        }

        String reAuthToken = UUID.randomUUID().toString().replace("-", "");
        String redisKey = "gmp:esign:" + reAuthToken;
        redisTemplate.opsForValue().set(redisKey, username, 5, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("esignToken", reAuthToken);
        result.put("expiresIn", "300");
        return Result.ok("二次认证通过", result);
    }

    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long xUserId,
                                                    @RequestHeader(value = "X-User-Name", required = false) String xUsername,
                                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 优先从请求头获取（网关转发时注入），否则从 JWT Token 解析
        Long userId = xUserId;
        String username = xUsername;
        if (userId == null && authHeader != null && authHeader.startsWith(SystemConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SystemConstants.TOKEN_PREFIX.length());
            userId = JwtUtils.getUserId(token);
            username = JwtUtils.getUsername(token);
        }
        if (userId == null || username == null) {
            return Result.fail(ResultCode.UNAUTHORIZED, "无法获取用户信息");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        // 从数据库查询用户详细信息
        try {
            var user = userDetailsService.getUserByUsername(username);
            if (user != null) {
                userInfo.put("displayName", user.getUserName());
                userInfo.put("deptId", user.getDeptId());
                userInfo.put("deptName", user.getDeptName());
            }
        } catch (Exception e) {
            log.warn("查询用户详情失败: {}", e.getMessage());
        }
        // 查询用户角色
        String roles = userDetailsService.getRolesByUsername(username);
        userInfo.put("roles", roles.isEmpty() ? new String[0] : roles.split(","));
        return Result.ok(userInfo);
    }

    @GetMapping("/user-menus")
    public Result<List<Map<String, Object>>> getUserMenus(@RequestHeader(value = "X-User-Name", required = false) String xUsername,
                                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = xUsername;
        if (username == null && authHeader != null && authHeader.startsWith(SystemConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SystemConstants.TOKEN_PREFIX.length());
            username = JwtUtils.getUsername(token);
        }
        if (username == null) {
            return Result.fail(ResultCode.UNAUTHORIZED, "无法获取用户信息");
        }
        return Result.ok(userDetailsService.getUserMenus(username));
    }

    private void saveLoginLog(String username, String result, String errorMsg, long start) {
        try {
            String ip = "unknown";
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                ip = sra.getRequest().getRemoteAddr();
            }

            SysOperLog operLog = new SysOperLog();
            operLog.setOperType("LOGIN");
            operLog.setModule("登录认证");
            operLog.setDescription("用户登录" + ("SUCCESS".equals(result) ? "成功" : "失败") + ": " + username);
            operLog.setRequestUrl("/auth/login");
            operLog.setRequestMethod("POST");
            operLog.setCostTime(System.currentTimeMillis() - start);
            operLog.setResult(result);
            operLog.setErrorMsg(errorMsg);
            operLog.setOperIp(ip);
            operLog.setOperUserName(username);
            operLog.setCreateTime(LocalDateTime.now());
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("登录日志记录失败: {}", e.getMessage());
        }
    }

    @lombok.Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String captchaKey;
        private String captchaCode;
    }

    @lombok.Data
    public static class RefreshTokenRequest {
        private String refreshToken;
        private String oldAccessToken;
    }

    @lombok.Data
    public static class ReAuthRequest {
        private String password;
        private String reason;
    }

    private static String generateCaptchaCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
