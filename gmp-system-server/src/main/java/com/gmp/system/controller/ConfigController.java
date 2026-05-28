package com.gmp.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysConfig;
import com.gmp.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/config")
public class ConfigController extends CommonController<SysConfigService, SysConfig> {

    private final SysConfigService sysConfigService;

    @Override
    protected SysConfigService getService() {
        return sysConfigService;
    }

    @GetMapping("/list")
    public Result<List<SysConfig>> listAll() {
        return success(sysConfigService.list());
    }

    @GetMapping("/list-by-category")
    public Result<List<SysConfig>> listByCategory(@RequestParam String category) {
        return success(sysConfigService.list(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCategory, category)));
    }

    @GetMapping("/page")
    public Result<PageResult<SysConfig>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return success(sysConfigService.pageQuery(pageNum, pageSize, null));
    }

    @GetMapping("/{id}")
    public Result<SysConfig> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<SysConfig> update(@RequestBody SysConfig config) {
        return updateEntity(config);
    }

    /**
     * 更新密码策略配置
     */
    @PutMapping("/password-policy")
    @PreAuthorize("hasAuthority('system:config:list')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePasswordPolicy(@RequestBody Map<String, String> policy) {
        updateConfigValue("password.level", policy.get("level"), "密码等级(low/medium/high)");
        updateConfigValue("password.min.length", policy.get("minLength"), "密码最小长度");
        updateConfigValue("password.max.length", policy.get("maxLength"), "密码最大长度");
        updateConfigValue("password.expire.days", policy.get("expireDays"), "密码过期天数");
        updateConfigValue("password.history.count", policy.get("historyCount"), "历史密码检查数量");
        log.info("密码策略已更新: {}", policy);
        return success();
    }

    private void updateConfigValue(String key, String value, String description) {
        if (value == null) return;
        SysConfig existing = sysConfigService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setDescription(description);
            sysConfigService.updateById(existing);
        } else {
            SysConfig config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCategory("security");
            config.setDescription(description);
            config.setIsSystem(true);
            sysConfigService.save(config);
        }
    }
}
