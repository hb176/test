package com.gmp.form.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gmp.framework.base.PageResult;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.form.entity.FormDefinition;
import com.gmp.form.entity.FormData;
import com.gmp.form.service.FormDataService;
import com.gmp.form.service.FormDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 表单管理控制器 - 表单定义与表单数据的CRUD接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/form")
public class FormController {

    private final FormDefinitionService formDefinitionService;
    private final FormDataService formDataService;

    // ==================== 表单定义管理 ====================

    @GetMapping("/definition/page")
    public Result<Map<String, Object>> queryDefinitionPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        var wrapper = new LambdaQueryWrapper<FormDefinition>();
        if (category != null && !category.isEmpty()) wrapper.eq(FormDefinition::getSysModule, category);
        if (status != null && !status.isEmpty()) wrapper.eq(FormDefinition::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) wrapper.and(w -> w
                .like(FormDefinition::getName, keyword).or().like(FormDefinition::getCode, keyword));
        wrapper.orderByDesc(FormDefinition::getCreateTime);
        PageResult<FormDefinition> page = formDefinitionService.pageQuery(pageNum, pageSize, wrapper);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        return Result.ok(data);
    }

    @GetMapping("/definition/{id}")
    public Result<FormDefinition> getDefinitionById(@PathVariable Long id) {
        FormDefinition def = formDefinitionService.getById(id);
        if (def == null) return Result.fail(ResultCode.NOT_FOUND, "表单定义不存在");
        return Result.ok(def);
    }

    @GetMapping("/definition/key/{formKey}")
    public Result<FormDefinition> getDefinitionByKey(@PathVariable String formKey) {
        FormDefinition def = formDefinitionService.getOne(
                new LambdaQueryWrapper<FormDefinition>().eq(FormDefinition::getCode, formKey));
        if (def == null) return Result.fail(ResultCode.NOT_FOUND, "表单定义不存在");
        return Result.ok(def);
    }

    @PreAuthorize("hasAuthority('form:definition:add')")
    @PostMapping("/definition")
    public Result<FormDefinition> createDefinition(@RequestBody FormDefinition definition) {
        // 检查编码唯一性
        if (definition.getCode() != null) {
            long count = formDefinitionService.lambdaQuery()
                    .eq(FormDefinition::getCode, definition.getCode()).count();
            if (count > 0) return Result.fail(ResultCode.VALIDATION_FAILED, "表单编码已存在");
        }
        definition.setStatus("NORM");
        formDefinitionService.save(definition);
        return Result.ok("创建成功", definition);
    }

    @PreAuthorize("hasAuthority('form:definition:edit')")
    @PutMapping("/definition/{id}")
    public Result<FormDefinition> updateDefinition(@PathVariable Long id,
                                                     @RequestBody FormDefinition definition) {
        FormDefinition existing = formDefinitionService.getById(id);
        if (existing == null) return Result.fail(ResultCode.NOT_FOUND, "表单定义不存在");
        definition.setId(id);
        formDefinitionService.updateById(definition);
        return Result.ok("更新成功", definition);
    }

    @PutMapping("/definition/{id}/publish")
    public Result<Void> publishDefinition(@PathVariable Long id) {
        FormDefinition def = formDefinitionService.getById(id);
        if (def == null) return Result.fail(ResultCode.NOT_FOUND, "表单定义不存在");
        def.setStatus("PUBLISHED");
        formDefinitionService.updateById(def);
        return Result.okMsg("发布成功");
    }

    @PutMapping("/definition/{id}/archive")
    public Result<Void> archiveDefinition(@PathVariable Long id) {
        FormDefinition def = formDefinitionService.getById(id);
        if (def == null) return Result.fail(ResultCode.NOT_FOUND, "表单定义不存在");
        def.setStatus("ARCHIVED");
        formDefinitionService.updateById(def);
        return Result.okMsg("归档成功");
    }

    @PreAuthorize("hasAuthority('form:definition:delete')")
    @DeleteMapping("/definition/{id}")
    public Result<Void> deleteDefinition(@PathVariable Long id) {
        formDefinitionService.removeById(id);
        return Result.okMsg("删除成功");
    }

    @PostMapping("/definition/{id}/copy")
    public Result<FormDefinition> copyDefinition(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        FormDefinition source = formDefinitionService.getById(id);
        if (source == null) return Result.fail(ResultCode.NOT_FOUND, "源表单不存在");

        FormDefinition copy = new FormDefinition();
        copy.setName(body.getOrDefault("name", source.getName() + " (副本)"));
        copy.setCode(body.get("formKey"));
        copy.setSysModule(source.getSysModule());
        copy.setModule(source.getModule());
        copy.setEditContent(source.getEditContent());
        copy.setViewContent(source.getViewContent());
        copy.setAddContent(source.getAddContent());
        copy.setJsContent(source.getJsContent());
        copy.setWorkflowKey(source.getWorkflowKey());
        copy.setFormRangeType(source.getFormRangeType());
        copy.setStatus("NORM");
        formDefinitionService.save(copy);
        return Result.ok("复制成功", copy);
    }

    // ==================== 表单数据管理 ====================

    @GetMapping("/data/page")
    public Result<Map<String, Object>> queryFormDataPage(
            @RequestParam String formKey,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dataStatus) {
        var wrapper = new LambdaQueryWrapper<FormData>()
                .eq(FormData::getFormKey, formKey);
        if (dataStatus != null && !dataStatus.isEmpty()) wrapper.eq(FormData::getDataStatus, dataStatus);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(FormData::getIndexedTitle, keyword);
        wrapper.orderByDesc(FormData::getCreateTime);
        PageResult<FormData> page = formDataService.pageQuery(pageNum, pageSize, wrapper);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        return Result.ok(data);
    }

    @GetMapping("/data/{id}")
    public Result<FormData> getFormDataById(@PathVariable Long id) {
        FormData data = formDataService.getById(id);
        if (data == null) return Result.fail(ResultCode.NOT_FOUND, "表单数据不存在");
        return Result.ok(data);
    }

    @PostMapping("/data")
    public Result<FormData> createFormData(@RequestBody FormData formData) {
        formData.setDataStatus("DRAFT");
        formDataService.save(formData);
        return Result.ok("保存成功", formData);
    }

    @PutMapping("/data/{id}")
    public Result<Void> updateFormData(@PathVariable Long id, @RequestBody FormData formData) {
        formData.setId(id);
        formDataService.updateById(formData);
        return Result.okMsg("更新成功");
    }

    @PutMapping("/data/{id}/submit")
    public Result<Void> submitFormData(@PathVariable Long id) {
        FormData data = formDataService.getById(id);
        if (data == null) return Result.fail(ResultCode.NOT_FOUND, "表单数据不存在");
        data.setDataStatus("SUBMITTED");
        data.setSubmitTime(java.time.LocalDateTime.now());
        formDataService.updateById(data);
        return Result.okMsg("提交成功");
    }

    @PreAuthorize("hasAuthority('form:data:delete')")
    @DeleteMapping("/data/{id}")
    public Result<Void> deleteFormData(@PathVariable Long id) {
        formDataService.removeById(id);
        return Result.okMsg("删除成功");
    }
}
