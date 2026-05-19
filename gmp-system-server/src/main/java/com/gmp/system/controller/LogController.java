package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.system.entity.SysOperLog;
import com.gmp.system.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/log")
public class LogController extends CommonController<SysOperLogService, SysOperLog> {

    private final SysOperLogService sysOperLogService;

    @Override
    protected SysOperLogService getService() {
        return sysOperLogService;
    }

    @GetMapping("/page")
    public Result<PageResult<SysOperLog>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String operType,
                                                @RequestParam(required = false) String keyword) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysOperLog>();
        if (operType != null) wrapper.eq(SysOperLog::getOperType, operType);
        if (keyword != null) wrapper.like(SysOperLog::getDescription, keyword);
        wrapper.orderByDesc(SysOperLog::getCreateTime);
        return success(sysOperLogService.pageQuery(pageNum, pageSize, wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysOperLog> getLogDetail(@PathVariable Long id) {
        return getById(id);
    }
}
