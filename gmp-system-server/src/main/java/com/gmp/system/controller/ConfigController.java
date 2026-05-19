package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysConfig;
import com.gmp.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result<SysConfig> update(@RequestBody SysConfig config) {
        return updateEntity(config);
    }
}
