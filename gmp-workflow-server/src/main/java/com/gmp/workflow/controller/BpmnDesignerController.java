package com.gmp.workflow.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * BPMN Designer API 适配 — 兼容 flow-admin-ui 设计器的接口协议
 *
 * 原接口路径: /flow/bpmnDesigner/prod/api/*
 * 返回格式: {code: 1, msg: "OK", data: ...}  (1=成功, 0=失败)
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/flow/bpmnDesigner/prod/api")
public class BpmnDesignerController {

    private static final String SUCCESS = "100";
    private static final String FAIL = "101";

    private final RepositoryService repositoryService;

    public BpmnDesignerController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    // ==================== BPMN 模型核心 ====================

    @GetMapping("/getFunctionVariableVos")
    public Map<String, Object> getFunctionVariableVos() {
        List<Map<String, Object>> list = new ArrayList<>();
        // 日期函数
        list.add(func("dateFormat", "日期格式化", "格式: dateFormat(date, pattern)", List.of(
                arg("date", "日期对象"),
                arg("pattern", "格式,如 yyyy-MM-dd"))));
        list.add(func("dateAdd", "日期加减", "格式: dateAdd(date, days)", List.of(
                arg("date", "日期对象"),
                arg("days", "天数,正数为加,负数为减"))));
        list.add(func("now", "当前时间", "格式: now()", List.of()));
        // 字符串函数
        list.add(func("concat", "字符串拼接", "格式: concat(s1, s2, ...)", List.of(
                arg("strings", "多个字符串参数"))));
        list.add(func("toUpperCase", "转大写", "格式: toUpperCase(s)", List.of(
                arg("s", "字符串"))));
        // 用户/组织函数
        list.add(func("getDeptLeader", "获取部门负责人", "格式: getDeptLeader(deptId)", List.of(
                arg("deptId", "部门ID"))));
        list.add(func("getDeptLeaders", "获取部门负责人链", "格式: getDeptLeaders(deptId)", List.of(
                arg("deptId", "部门ID"))));
        return ok(list);
    }

    @PostMapping("/validateBpmnModel")
    public Map<String, Object> validateBpmnModel(@RequestBody Map<String, Object> body) {
        String xml = (String) body.getOrDefault("modelXml", "");
        try {
            repositoryService.createDeployment()
                    .addString("validate.bpmn", xml)
                    .deploy();
            Map<String, Object> ok = ok(true);
            ok.put("msg", "校验通过");
            return ok;
        } catch (Exception e) {
            return fail("校验失败: " + e.getMessage());
        }
    }

    @PostMapping("/saveBpmnModel")
    public Map<String, Object> saveBpmnModel(@RequestBody Map<String, Object> body) {
        try {
            String modelId = (String) body.get("modelId");
            String modelName = (String) body.get("fileName");
            if (modelName == null || modelName.isEmpty()) modelName = (String) body.get("modelName");
            if (modelName == null || modelName.isEmpty()) modelName = "未命名流程";
            String xml = (String) body.get("modelXml");
            String modelKey = (String) body.get("modelKey");
            if (modelKey == null || modelKey.isEmpty()) modelKey = modelName;
            String category = (String) body.get("category");

            log.info("保存BPMN模型: modelId={}, name={}, key={}, xmlLen={}",
                    modelId, modelName, modelKey, xml != null ? xml.length() : 0);

            if (xml == null || xml.isEmpty()) {
                return fail("BPMN XML不能为空");
            }

            // 创建或更新 Model
            Model model;
            if (modelId != null && !modelId.isEmpty()) {
                model = repositoryService.getModel(modelId);
                if (model == null) {
                    model = repositoryService.newModel();
                }
            } else {
                model = repositoryService.newModel();
            }
            model.setName(modelName);
            model.setKey(modelKey != null && !modelKey.isEmpty() ? modelKey : model.getId());
            model.setCategory(category != null && !category.isEmpty() ? category : "COMM");
            repositoryService.saveModel(model);

            // 保存 BPMN XML 到模型
            repositoryService.addModelEditorSource(model.getId(),
                    xml.getBytes(StandardCharsets.UTF_8));

            // 部署流程定义
            String deployMsg = "";
            try {
                repositoryService.createDeployment()
                        .addString(modelName + ".bpmn20.xml", xml)
                        .name(modelName)
                        .category(category)
                        .deploy();
                deployMsg = "，流程已自动部署";
            } catch (Exception deployEx) {
                log.warn("部署流程失败(模型已保存): {}", deployEx.getMessage());
                deployMsg = "，但部署失败: " + deployEx.getMessage();
            }

            log.info("BPMN模型已保存: modelId={}, name={}", model.getId(), model.getName());
            Map<String, Object> ok = ok(model.getId());
            ok.put("msg", "保存成功！" + deployMsg);
            return ok;
        } catch (Exception e) {
            log.error("保存BPMN模型失败", e);
            return fail("保存失败: " + e.getMessage());
        }
    }

    @GetMapping("/getBpmnByModelId/{modelId}")
    public Map<String, Object> getBpmnByModelId(@PathVariable String modelId) {
        try {
            Model model = repositoryService.getModel(modelId);
            if (model == null) return fail("模型不存在: " + modelId);

            byte[] source = repositoryService.getModelEditorSource(modelId);
            String xml = source != null ? new String(source, StandardCharsets.UTF_8) : "";

            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("modelId", model.getId());
            vo.put("modelKey", model.getKey());
            vo.put("modelName", model.getName());
            vo.put("modelXml", xml);
            vo.put("fileName", model.getName());
            vo.put("category", model.getCategory());
            return ok(vo);
        } catch (Exception e) {
            return fail("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/getBpmnByModelKey/{modelKey}")
    public Map<String, Object> getBpmnByModelKey(@PathVariable String modelKey) {
        List<Model> models = repositoryService.createModelQuery().modelKey(modelKey).list();
        if (models.isEmpty()) return fail("模型不存在: " + modelKey);
        return getBpmnByModelId(models.get(0).getId());
    }

    // ==================== 组织架构 ====================

    @GetMapping("/getOrgTree")
    public Map<String, Object> getOrgTree() {
        // TODO: 调用 system-server 获取组织树
        return ok(Collections.emptyList());
    }

    @PostMapping("/getPersonalPagerModel")
    public Map<String, Object> getPersonalPagerModel(@RequestBody Map<String, Object> params) {
        return ok(new LinkedHashMap<>() {{
            put("rows", Collections.emptyList());
            put("total", 0);
        }});
    }

    @PostMapping("/getPersonalsByRole")
    public Map<String, Object> getPersonalsByRole(@RequestBody Map<String, Object> params) {
        return ok(Collections.emptyList());
    }

    @PostMapping("/getCompanies")
    public Map<String, Object> getCompanies(@RequestBody Map<String, Object> params) {
        return ok(Collections.emptyList());
    }

    @PostMapping("/getRolePagerModel")
    public Map<String, Object> getRolePagerModel(@RequestBody Map<String, Object> params) {
        return ok(new LinkedHashMap<>() {{
            put("rows", Collections.emptyList());
            put("total", 0);
        }});
    }

    // ==================== 基础数据 ====================

    @PostMapping("/getCategories")
    public Map<String, Object> getCategories(@RequestBody Map<String, Object> params) {
        List<Map<String, String>> categories = new ArrayList<>();
        categories.add(cat("COMM", "通用流程"));
        categories.add(cat("QMS", "质量过程管理"));
        categories.add(cat("DMS", "文件管理系统"));
        categories.add(cat("TMS", "培训管理系统"));
        categories.add(cat("QRS", "质量回顾系统"));
        return ok(categories);
    }

    @PostMapping("/getModelInfoPagerModel")
    public Map<String, Object> getModelInfoPagerModel(@RequestBody Map<String, Object> params) {
        List<Model> models = repositoryService.createModelQuery()
                .orderByLastUpdateTime().desc().list();
        List<Map<String, Object>> rows = models.stream().map(m -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", m.getId());
            row.put("modelId", m.getId());
            row.put("key", m.getKey());
            row.put("name", m.getName());
            row.put("category", m.getCategory());
            row.put("version", m.getVersion());
            row.put("createTime", m.getCreateTime());
            row.put("lastUpdateTime", m.getLastUpdateTime());
            return row;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", rows.size());
        return ok(result);
    }

    // ==================== 监听器 ====================

    @PostMapping("/getListenersAndParams")
    public Map<String, Object> getListenersAndParams(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> listeners = new ArrayList<>();
        listeners.add(listener("logExecutionListener", "日志监听器",
                List.of(param("level", "日志级别", "INFO/DEBUG/WARN"))));
        listeners.add(listener("assigneeListener", "审批人分配监听器",
                List.of(param("roleCode", "角色编码", ""))));
        listeners.add(listener("autoCompleteListener", "自动完成任务监听器",
                List.of(param("condition", "完成条件表达式", ""))));
        return ok(listeners);
    }

    // ==================== 变量（暂存） ====================

    @GetMapping("/getFormVariablesByCode/{formCode}")
    public Map<String, Object> getFormVariablesByCode(@PathVariable String formCode) {
        return ok(Collections.emptyList());
    }

    @GetMapping("/getRoleVariablesByOrgId/{orgId}/{flag}")
    public Map<String, Object> getRoleVariablesByOrgId(@PathVariable String orgId, @PathVariable String flag) {
        return ok(Collections.emptyList());
    }

    @GetMapping("/getDeptVariables")
    public Map<String, Object> getDeptVariables() { return ok(Collections.emptyList()); }

    @GetMapping("/getBaseVariables")
    public Map<String, Object> getBaseVariables() { return ok(Collections.emptyList()); }

    @GetMapping("/getCompanyVariables")
    public Map<String, Object> getCompanyVariables() { return ok(Collections.emptyList()); }

    @GetMapping("/getMatrixDeptVariables")
    public Map<String, Object> getMatrixDeptVariables() { return ok(Collections.emptyList()); }

    @GetMapping("/getMatrixCompanyVariables")
    public Map<String, Object> getMatrixCompanyVariables() { return ok(Collections.emptyList()); }

    @PostMapping("/getMatrixRoles/{roleType}")
    public Map<String, Object> getMatrixRoles(@PathVariable Integer roleType) {
        return ok(Collections.emptyList());
    }

    @GetMapping("/getTaskFormInfoByModelKey/{modelKey}")
    public Map<String, Object> getTaskFormInfoByModelKey(@PathVariable String modelKey) {
        return ok(Collections.emptyList());
    }

    // ==================== 流程追踪 ====================

    @GetMapping("/getOneActivityVoByProcessInstanceIdAndActivityId/{instanceId}/{activityId}")
    public Map<String, Object> getOneActivityVo(@PathVariable String instanceId, @PathVariable String activityId) {
        return ok(Collections.emptyMap());
    }

    @GetMapping("/getHighLightedNodeVoByProcessInstanceId/{instanceId}")
    public Map<String, Object> getHighLightedNodeVo(@PathVariable String instanceId) {
        return ok(Collections.emptyMap());
    }

    // ==================== DMN (暂存) ====================

    @PostMapping("/getDmnPagerModel")
    public Map<String, Object> getDmnPagerModel(@RequestBody Map<String, Object> params) {
        return ok(Collections.emptyMap());
    }

    @GetMapping("/getDmnByModelId/{modelId}")
    public Map<String, Object> getDmnByModelId(@PathVariable String modelId) {
        return ok(Collections.emptyMap());
    }

    @GetMapping("/getDmnByModelKey/{modelKey}")
    public Map<String, Object> getDmnByModelKey(@PathVariable String modelKey) {
        return ok(Collections.emptyMap());
    }

    // ==================== 响应工具 ====================

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", SUCCESS);
        result.put("msg", "操作成功");
        result.put("data", data);
        return result;
    }

    private Map<String, Object> fail(String msg) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", FAIL);
        result.put("msg", msg);
        result.put("data", null);
        return result;
    }

    // ==================== 辅助构建器 ====================

    private Map<String, Object> func(String name, String label, String desc, List<Map<String, Object>> args) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("name", name);
        f.put("label", label);
        f.put("description", desc);
        f.put("arguments", args);
        return f;
    }

    private Map<String, Object> arg(String name, String desc) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("name", name);
        a.put("description", desc);
        return a;
    }

    private Map<String, Object> listener(String event, String name, List<Map<String, Object>> params) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("listenerEvent", event);
        l.put("listenerName", name);
        l.put("params", params);
        return l;
    }

    private Map<String, Object> param(String name, String label, String placeholder) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("name", name);
        p.put("label", label);
        p.put("placeholder", placeholder);
        return p;
    }

    private Map<String, String> cat(String code, String name) {
        Map<String, String> c = new LinkedHashMap<>();
        c.put("id", code);
        c.put("code", code);
        c.put("name", name);
        return c;
    }
}
