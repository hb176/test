package com.gmp.auth.controller;

import com.gmp.auth.service.TokenConfigService;
import com.gmp.framework.base.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TokenConfigController {

    private final TokenConfigService tokenConfigService;

    @GetMapping("/token-expire-config")
    public Result<Map<String, Object>> getConfig() {
        long minutes = tokenConfigService.getExpireMinutes();
        return Result.ok(Map.of("expireMinutes", minutes));
    }

    @PutMapping("/token-expire-config")
    public Result<Void> updateConfig(@RequestBody UpdateConfigRequest req) {
        if (req.getExpireMinutes() == null || req.getExpireMinutes() < 1 || req.getExpireMinutes() > 1440) {
            return Result.fail(400, "过期时间必须在1-1440分钟之间");
        }
        tokenConfigService.setExpireMinutes(req.getExpireMinutes());
        return Result.okMsg("更新成功");
    }

    @Data
    public static class UpdateConfigRequest {
        private Long expireMinutes;
    }
}
