package com.gmp.system.controller;

import com.gmp.framework.base.CommonController;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.system.entity.SysMessageTemplate;
import com.gmp.system.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/message-template")
public class SysMessageTemplateController extends CommonController<SysMessageTemplateService, SysMessageTemplate> {

    private final SysMessageTemplateService templateService;

    @Override
    protected SysMessageTemplateService getService() {
        return templateService;
    }

    @GetMapping("/list")
    public Result<?> queryList(@RequestParam(required = false) String sysModule) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessageTemplate>();
        if (sysModule != null && !sysModule.isEmpty()) {
            wrapper.eq(SysMessageTemplate::getSysModule, sysModule);
        }
        wrapper.orderByAsc(SysMessageTemplate::getSysModule, SysMessageTemplate::getTemplateCode);
        return Result.ok(templateService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<SysMessageTemplate> getById(@PathVariable Long id) {
        SysMessageTemplate tpl = templateService.getById(id);
        if (tpl == null) return Result.fail(ResultCode.NOT_FOUND, "模板不存在");
        return Result.ok(tpl);
    }

    @PreAuthorize("hasAuthority('system:template:add')")
    @PostMapping
    public Result<SysMessageTemplate> create(@RequestBody SysMessageTemplate template) {
        if (template.getTemplateCode() != null) {
            long count = templateService.lambdaQuery()
                    .eq(SysMessageTemplate::getTemplateCode, template.getTemplateCode()).count();
            if (count > 0) return Result.fail(ResultCode.VALIDATION_FAILED, "模板编码已存在");
        }
        templateService.save(template);
        return Result.ok("创建成功", template);
    }

    @PreAuthorize("hasAuthority('system:template:edit')")
    @PutMapping("/{id}")
    public Result<SysMessageTemplate> update(@PathVariable Long id, @RequestBody SysMessageTemplate template) {
        SysMessageTemplate existing = templateService.getById(id);
        if (existing == null) return Result.fail(ResultCode.NOT_FOUND, "模板不存在");
        template.setId(id);
        templateService.updateById(template);
        return Result.ok("更新成功", template);
    }

    @PreAuthorize("hasAuthority('system:template:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.removeById(id);
        return Result.okMsg("删除成功");
    }
}
