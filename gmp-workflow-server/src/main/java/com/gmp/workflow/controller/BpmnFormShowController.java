package com.gmp.workflow.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * BPMN Designer 表单展示适配 API
 */
@Slf4j
@RestController
@RequestMapping("/flow/form/show")
public class BpmnFormShowController {

    private static final String SUCCESS = "100";

    @GetMapping({"/getFormItemByFormCode/{version}/{formCode}", "/getFormItemByFormCode/{formCode}"})
    public Map<String, Object> getFormItemByFormCode(
            @PathVariable(required = false) Integer version,
            @PathVariable String formCode) {
        if (version == null) version = 0;
        log.debug("查询表单字段: formCode={}, version={}", formCode, version);
        return ok(Collections.emptyList());
    }

    @GetMapping("/getFormItemByFormKeyAndVersion/{formKey}/{version}")
    public Map<String, Object> getFormItemByFormKeyAndVersion(
            @PathVariable String formKey, @PathVariable int version) {
        log.debug("查询表单字段: formKey={}, version={}", formKey, version);
        return ok(Collections.emptyList());
    }

    @PostMapping("/getFormItemShowsByActivityId")
    public Map<String, Object> getFormItemShowsByActivityId(@RequestBody Map<String, Object> params) {
        log.debug("查询活动表单展示");
        return ok(Collections.emptyList());
    }

    @GetMapping("/getFormList")
    public Map<String, Object> getFormList() {
        List<Map<String, String>> forms = new ArrayList<>();
        forms.add(form("DEVIATION_FORM", "偏差报告表单"));
        forms.add(form("CAPA_FORM", "CAPA表单"));
        return ok(forms);
    }

    private Map<String, String> form(String code, String name) {
        Map<String, String> f = new LinkedHashMap<>();
        f.put("code", code);
        f.put("name", name);
        return f;
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", SUCCESS);
        result.put("msg", "操作成功");
        result.put("data", data);
        return result;
    }
}
