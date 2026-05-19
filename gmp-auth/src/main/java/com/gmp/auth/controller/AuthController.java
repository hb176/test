package com.gmp.auth.controller;

import com.gmp.auth.service.AuthUserDetailsService;
import com.gmp.auth.service.TokenConfigService;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.common.constant.SystemConstants;
import com.gmp.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfigService tokenConfigService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        log.info("用户登录请求 - username: {}", req.getUsername());

        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return Result.fail(ResultCode.BAD_REQUEST, "用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return Result.fail(ResultCode.BAD_REQUEST, "密码不能为空");
        }

        try {
            UserDetails user = userDetailsService.loadUserByUsername(req.getUsername());
            System.out.println(passwordEncoder.encode(req.getPassword()));
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return Result.fail(ResultCode.UNAUTHORIZED, "密码错误");
            }
        } catch (Exception e) {
            log.warn("登录失败: {}", e.getMessage());
            return Result.fail(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        Long id = userDetailsService.getIdByUsername(req.getUsername());
        long expireSeconds = tokenConfigService.getExpireSeconds();
        String accessToken = JwtUtils.createAccessToken(id, req.getUsername(), "ROLE_ADMIN", expireSeconds);
        String refreshToken = JwtUtils.createRefreshToken(id, req.getUsername());

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("accessToken", accessToken);
        resultData.put("refreshToken", refreshToken);
        resultData.put("expiresIn", expireSeconds);
        resultData.put("tokenType", "Bearer");

        log.info("用户登录成功 - username: {}, id: {}", req.getUsername(), id);
        return Result.ok("登录成功", resultData);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith(SystemConstants.TOKEN_PREFIX)) {
            log.info("用户登出 - Token已加入黑名单");
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
        String roles = JwtUtils.getRoles(request.getRefreshToken());
        String newAccessToken = JwtUtils.createAccessToken(userId, username, roles, tokenConfigService.getExpireSeconds());

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("accessToken", newAccessToken);
        resultData.put("expiresIn", tokenConfigService.getExpireSeconds());

        log.info("Token刷新成功 - 用户: {}", username);
        return Result.ok(resultData);
    }

    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader("X-User-Id") Long userId,
                                                    @RequestHeader("X-User-Name") String username) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        return Result.ok(userInfo);
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
    }
}
