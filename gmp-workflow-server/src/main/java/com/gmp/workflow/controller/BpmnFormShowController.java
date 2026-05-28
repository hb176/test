package com.gmp.workflow.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gmp.workflow.feign.FormFeignClient;
import com.gmp.workflow.model.flowable.WfActivityFormField;
import com.gmp.workflow.service.flowable.IWfActivityFormFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * BPMN Designer 表单展示适配 API
 *
 * 设计器通过此控制器获取可绑定的表单列表和表单字段定义
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow/form/show")
public class BpmnFormShowController {

    private static final String SUCCESS = "100";

    private final FormFeignClient formFeignClient;
    private final IWfActivityFormFieldService activityFormFieldService;

    @GetMapping({"/getFormItemByFormCode/{version}/{formCode}", "/getFormItemByFormCode/{formCode}"})
    public Map<String, Object> getFormItemByFormCode(
            @PathVariable(required = false) Integer version,
            @PathVariable String formCode) {
        try {
            var result = formFeignClient.getDefinitionByKey(formCode);
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> def = result.getData();
                String editContent = (String) def.get("editContent");
                if (editContent != null && !editContent.isEmpty()) {
                    JSONObject schema = JSON.parseObject(editContent);
                    JSONArray fields = schema.getJSONArray("fields");
                    if (fields != null) {
                        List<Map<String, Object>> items = new ArrayList<>();
                        for (int i = 0; i < fields.size(); i++) {
                            JSONObject f = fields.getJSONObject(i);
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("code", f.getString("fieldKey"));
                            item.put("name", f.getString("fieldName"));
                            item.put("type", f.getString("fieldType"));
                            item.put("required", f.getBooleanValue("required"));
                            item.put("placeholder", f.getString("placeholder"));
                            item.put("key", f.getString("fieldKey"));
                            item.put("operateType", 1);
                            items.add(item);
                        }
                        return ok(items);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取表单字段失败: formCode={}, error={}", formCode, e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @PostMapping("/getFormItemShowsByActivityId")
    public Map<String, Object> getFormItemShowsByActivityId(@RequestBody Map<String, Object> params) {
        String processKey = (String) params.get("processKey");
        String activityId = (String) params.get("activityId");
        String formCode = (String) params.get("formCode");

        List<Map<String, Object>> result = new ArrayList<>();

        // 先获取表单字段定义
        List<Map<String, Object>> formFields = Collections.emptyList();
        if (formCode != null) {
            Map<String, Object> formResult = getFormItemByFormCode(null, formCode);
            Object data = formResult.get("data");
            if (data instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cast = (List<Map<String, Object>>) list;
                formFields = cast;
            }
        }

        // 再获取已有的权限配置
        Map<String, WfActivityFormField> configMap = new HashMap<>();
        if (processKey != null && activityId != null) {
            List<WfActivityFormField> configs = activityFormFieldService.getByProcessKeyAndActivityId(processKey, activityId);
            for (WfActivityFormField c : configs) {
                configMap.put(c.getFieldKey(), c);
            }
        }

        // 合并：字段定义 + 权限配置
        for (Map<String, Object> field : formFields) {
            String fieldKey = (String) field.get("code");
            Map<String, Object> item = new LinkedHashMap<>(field);
            WfActivityFormField config = configMap.get(fieldKey);
            if (config != null) {
                item.put("readonly", Boolean.TRUE.equals(config.getReadonlyFlag()));
                item.put("hidden", Boolean.TRUE.equals(config.getHiddenFlag()));
                item.put("required", Boolean.TRUE.equals(config.getRequiredFlag()));
            } else {
                item.put("readonly", false);
                item.put("hidden", false);
                item.put("required", field.getOrDefault("required", false));
            }
            result.add(item);
        }

        return ok(result);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/saveOne")
    public Map<String, Object> saveOne(@RequestBody Map<String, Object> params) {
        String processKey = (String) params.get("processKey");
        String activityId = (String) params.get("activityId");
        List<Map<String, Object>> fieldConfigs = (List<Map<String, Object>>) params.get("fields");

        if (processKey == null || activityId == null || fieldConfigs == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("msg", "参数不完整");
            return err;
        }

        List<WfActivityFormField> entities = new ArrayList<>();
        for (Map<String, Object> fc : fieldConfigs) {
            WfActivityFormField e = new WfActivityFormField();
            e.setFieldKey((String) fc.get("fieldKey"));
            e.setFieldName((String) fc.get("fieldName"));
            e.setReadonlyFlag(Boolean.TRUE.equals(fc.get("readonly")));
            e.setHiddenFlag(Boolean.TRUE.equals(fc.get("hidden")));
            e.setRequiredFlag(Boolean.TRUE.equals(fc.get("required")));
            entities.add(e);
        }

        activityFormFieldService.saveFieldConfigs(processKey, activityId, entities);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/getFormList")
    public Map<String, Object> getFormList() {
        try {
            var result = formFeignClient.getDefinitionPage(1, 100);
            log.info("getFormList Feign响应: success={}, data={}", result.isSuccess(), result.getData());
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> pageData = result.getData();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> records = (List<Map<String, Object>>) pageData.getOrDefault("records", List.of());
                log.info("getFormList 记录数: {}", records.size());
                List<Map<String, Object>> forms = records.stream()
                        .filter(d -> !"ARCHIVED".equals(d.get("status")))
                        .map(d -> {
                            Map<String, Object> f = new LinkedHashMap<>();
                            f.put("code", d.get("code"));
                            f.put("name", d.get("name"));
                            return f;
                        }).toList();
                log.info("getFormList 返回表单数: {}", forms.size());
                return ok(forms);
            }
            log.warn("getFormList Feign调用失败: success={}", result.isSuccess());
        } catch (Exception e) {
            log.error("getFormList 异常: {}", e.getMessage(), e);
        }
        return ok(Collections.emptyList());
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", SUCCESS);
        result.put("msg", "操作成功");
        result.put("data", data);
        return result;
    }
}
