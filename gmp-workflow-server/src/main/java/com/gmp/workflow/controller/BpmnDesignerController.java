package com.gmp.workflow.controller;

import com.gmp.framework.base.Result;
import com.gmp.workflow.feign.FormFeignClient;
import com.gmp.workflow.feign.SystemFeignClient;
import com.gmp.workflow.model.flowable.FlowListener;
import com.gmp.workflow.model.flowable.ModelInfo;
import com.gmp.workflow.service.flowable.IFlowListenerService;
import com.gmp.workflow.service.flowable.IFlowProcessDiagramService;
import com.gmp.workflow.service.flowable.IFlowableBpmnService;
import com.gmp.workflow.service.flowable.IModelInfoService;
import com.gmp.workflow.vo.flowable.model.HighLightedNodeVo;
import com.gmp.workflow.vo.flowable.model.ModelInfoVo;
import com.gmp.workflow.vo.flowable.task.ActivityVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.web.bind.annotation.*;

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
@RequiredArgsConstructor
@RequestMapping("/flow/bpmnDesigner/prod/api")
public class BpmnDesignerController {

    private static final String SUCCESS = "100";
    private static final String FAIL = "101";

    private final RepositoryService repositoryService;
    private final SystemFeignClient systemFeignClient;
    private final IModelInfoService modelInfoService;
    private final IFlowProcessDiagramService flowProcessDiagramService;
    private final IFlowListenerService flowListenerService;
    private final IFlowableBpmnService flowableBpmnService;
    private final FormFeignClient formFeignClient;

    // ==================== BPMN 模型核心 ====================

    @GetMapping("/getFunctionVariableVos")
    public Map<String, Object> getFunctionVariableVos() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(func("dateFormat", "日期格式化", "格式: dateFormat(date, pattern)", List.of(
                arg("date", "日期对象"),
                arg("pattern", "格式,如 yyyy-MM-dd"))));
        list.add(func("dateAdd", "日期加减", "格式: dateAdd(date, days)", List.of(
                arg("date", "日期对象"),
                arg("days", "天数,正数为加,负数为减"))));
        list.add(func("now", "当前时间", "格式: now()", List.of()));
        list.add(func("concat", "字符串拼接", "格式: concat(s1, s2, ...)", List.of(
                arg("strings", "多个字符串参数"))));
        list.add(func("toUpperCase", "转大写", "格式: toUpperCase(s)", List.of(
                arg("s", "字符串"))));
        list.add(func("getDeptLeader", "获取部门负责人", "格式: getDeptLeader(deptId)", List.of(
                arg("deptId", "部门ID"))));
        list.add(func("getDeptLeaders", "获取部门负责人链", "格式: getDeptLeaders(deptId)", List.of(
                arg("deptId", "部门ID"))));
        return ok(list);
    }

    @PostMapping("/validateBpmnModel")
    public Map<String, Object> validateBpmnModel(@RequestBody Map<String, Object> body) {
        String xml = (String) body.getOrDefault("modelXml", "");
        Result<String> result = flowableBpmnService.validateBpmnXml(xml);
        if (result.isSuccess()) {
            Map<String, Object> r = ok(true);
            r.put("msg", result.getData());
            return r;
        }
        return fail(result.getMessage());
    }

    @PostMapping("/saveBpmnModel")
    public Map<String, Object> saveBpmnModel(@RequestBody Map<String, Object> body) {
        Result<String> result = flowableBpmnService.saveBpmnModel(body);
        if (result.isSuccess()) {
            Map<String, Object> r = ok(result.getData());
            r.put("msg", result.getMessage());
            r.put("success", true);
            return r;
        }
        Map<String, Object> r = fail(result.getMessage());
        r.put("success", false);
        return r;
    }

    @GetMapping("/getBpmnByModelId/{modelId}")
    public Map<String, Object> getBpmnByModelId(@PathVariable String modelId) {
        ModelInfoVo vo = flowableBpmnService.loadBpmnXmlByModelId(modelId);
        if (vo == null) return fail("模型不存在: " + modelId);
        return ok(vo);
    }

    @GetMapping("/getBpmnByModelKey/{modelKey}")
    public Map<String, Object> getBpmnByModelKey(@PathVariable String modelKey) {
        ModelInfoVo vo = flowableBpmnService.loadBpmnXmlByModelKey(modelKey);
        if (vo == null) return fail("模型不存在: " + modelKey);
        return ok(vo);
    }

    // ==================== 组织架构 ====================

    @SuppressWarnings("unchecked")
    @GetMapping("/getOrgTree")
    public Map<String, Object> getOrgTree() {
        try {
            var result = systemFeignClient.getDeptTree();
            if (result.isSuccess() && result.getData() != null) {
                List<Map<String, Object>> depts = result.getData();
                // 设计器需要 [{id, pid, name}] 扁平结构
                List<Map<String, Object>> flat = new ArrayList<>();
                flattenDeptTree(depts, 0L, flat);
                return ok(flat);
            }
        } catch (Exception e) {
            log.warn("查询部门树失败: {}", e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    private void flattenDeptTree(List<Map<String, Object>> nodes, Long parentId, List<Map<String, Object>> result) {
        for (Map<String, Object> node : nodes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", node.get("id"));
            item.put("pid", node.get("parentId"));
            item.put("name", node.get("deptName"));
            result.add(item);
            Object children = node.get("children");
            if (children instanceof List<?> childList && !childList.isEmpty()) {
                flattenDeptTree((List<Map<String, Object>>) children, (Long) node.get("id"), result);
            }
        }
    }

    @PostMapping("/getPersonalPagerModel")
    public Map<String, Object> getPersonalPagerModel(@RequestBody Map<String, Object> params) {
        try {
            // 提取分页和搜索参数
            Map<String, Object> query = getMapParam(params, "query");
            Map<String, Object> entity = getMapParam(params, "entity");
            int pageNum = getIntParam(query, "pageNum", 1);
            int pageSize = getIntParam(query, "pageSize", 20);
            String keyword = entity != null ? (String) entity.get("keyword") : null;

            var result = systemFeignClient.getUserPage(pageNum, pageSize, keyword);
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> pageData = result.getData();
                List<Map<String, Object>> records = (List<Map<String, Object>>) pageData.getOrDefault("records", List.of());
                List<Map<String, Object>> rows = records.stream().map(u -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", u.get("id"));
                    row.put("name", u.get("userName"));
                    row.put("code", u.get("userId"));
                    row.put("deptId", u.get("deptId"));
                    row.put("deptName", u.get("deptName"));
                    return row;
                }).toList();
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("rows", rows);
                data.put("total", pageData.getOrDefault("total", 0));
                return ok(data);
            }
        } catch (Exception e) {
            log.warn("查询用户列表失败: {}", e.getMessage());
        }
        return ok(new LinkedHashMap<>() {{ put("rows", Collections.emptyList()); put("total", 0); }});
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/getPersonalsByRole")
    public Map<String, Object> getPersonalsByRole(@RequestBody Map<String, Object> params) {
        try {
            String roleCode = params != null ? (String) params.get("roleCode") : null;
            if (roleCode == null || roleCode.isEmpty()) return ok(Collections.emptyList());

            var result = systemFeignClient.getUsersByRole(roleCode);
            if (result.isSuccess() && result.getData() != null) {
                List<Map<String, Object>> users = result.getData();
                return ok(users.stream().map(u -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", u.get("id"));
                    item.put("name", u.get("userName"));
                    item.put("code", u.get("userId"));
                    return item;
                }).toList());
            }
        } catch (Exception e) {
            log.warn("按角色查询用户失败: {}", e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @PostMapping("/getCompanies")
    public Map<String, Object> getCompanies(@RequestBody Map<String, Object> params) {
        // 暂无公司概念，返回空
        return ok(Collections.emptyList());
    }

    @PostMapping("/getRolePagerModel")
    public Map<String, Object> getRolePagerModel(@RequestBody Map<String, Object> params) {
        try {
            var result = systemFeignClient.getRoleList();
            if (result.isSuccess() && result.getData() != null) {
                List<Map<String, Object>> roles = result.getData();
                List<Map<String, Object>> rows = roles.stream().map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", r.get("id"));
                    row.put("name", r.get("roleName"));
                    row.put("sn", r.get("roleCode"));
                    return row;
                }).toList();
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("rows", rows);
                data.put("total", rows.size());
                return ok(data);
            }
        } catch (Exception e) {
            log.warn("查询角色列表失败: {}", e.getMessage());
        }
        return ok(new LinkedHashMap<>() {{ put("rows", Collections.emptyList()); put("total", 0); }});
    }

    private Map<String, Object> getMapParam(Map<String, Object> params, String key) {
        if (params == null) return null;
        Object val = params.get(key);
        return val instanceof Map ? (Map<String, Object>) val : null;
    }

    private int getIntParam(Map<String, Object> params, String key, int defaultVal) {
        if (params == null) return defaultVal;
        Object val = params.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultVal;
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
        List<Map<String, Object>> rows = flowableBpmnService.getModelList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", rows);
        result.put("total", rows.size());
        return ok(result);
    }

    // ==================== 监听器 ====================

    @PostMapping("/getListenersAndParams")
    public Map<String, Object> getListenersAndParams(@RequestBody Map<String, Object> params) {
        try {
            // 优先从数据库查询监听器
            FlowListener queryCondition = new FlowListener();
            if (params != null && params.containsKey("listenerType")) {
                queryCondition.setListenerType((String) params.get("listenerType"));
            }
            var listeners = flowListenerService.getListAndParams(queryCondition);
            if (listeners != null && !listeners.isEmpty()) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (var l : listeners) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("listenerEvent", l.getEventType());
                    item.put("listenerName", l.getName());
                    item.put("listenerType", l.getListenerType());
                    item.put("value", l.getValue());
                    item.put("remark", l.getRemark());
                    // 参数列表
                    List<Map<String, Object>> paramList = new ArrayList<>();
                    if (l.getParams() != null) {
                        for (var p : l.getParams()) {
                            paramList.add(param(p.getName(), p.getRemark() != null ? p.getRemark() : p.getName(), p.getValue()));
                        }
                    }
                    item.put("params", paramList);
                    result.add(item);
                }
                return ok(result);
            }
        } catch (Exception e) {
            log.warn("从数据库查询监听器失败，使用默认数据: {}", e.getMessage());
        }

        // 数据库无数据时返回默认监听器
        List<Map<String, Object>> listeners = new ArrayList<>();
        listeners.add(listener("logExecutionListener", "日志监听器",
                List.of(param("level", "日志级别", "INFO/DEBUG/WARN"))));
        listeners.add(listener("assigneeListener", "审批人分配监听器",
                List.of(param("roleCode", "角色编码", ""))));
        listeners.add(listener("autoCompleteListener", "自动完成任务监听器",
                List.of(param("condition", "完成条件表达式", ""))));
        return ok(listeners);
    }

    // ==================== 变量 ====================

    @GetMapping("/getFormVariablesByCode/{formCode}")
    public Map<String, Object> getFormVariablesByCode(@PathVariable String formCode) {
        try {
            var result = formFeignClient.getDefinitionByKey(formCode);
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> def = result.getData();
                String editContent = (String) def.get("editContent");
                if (editContent != null && !editContent.isEmpty()) {
                    com.alibaba.fastjson2.JSONObject schema = com.alibaba.fastjson2.JSON.parseObject(editContent);
                    com.alibaba.fastjson2.JSONArray fields = schema.getJSONArray("fields");
                    if (fields != null) {
                        List<Map<String, Object>> vars = new ArrayList<>();
                        for (int i = 0; i < fields.size(); i++) {
                            com.alibaba.fastjson2.JSONObject f = fields.getJSONObject(i);
                            Map<String, Object> v = new LinkedHashMap<>();
                            String key = f.getString("fieldKey");
                            v.put("name", "formData." + key);
                            v.put("label", f.getString("fieldName"));
                            v.put("type", f.getString("fieldType"));
                            vars.add(v);
                        }
                        return ok(vars);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取表单变量失败: formCode={}, error={}", formCode, e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @GetMapping("/getRoleVariablesByOrgId/{orgId}/{flag}")
    public Map<String, Object> getRoleVariablesByOrgId(@PathVariable String orgId, @PathVariable String flag) {
        try {
            var result = systemFeignClient.getRoleList();
            if (result.isSuccess() && result.getData() != null) {
                List<Map<String, Object>> vars = new ArrayList<>();
                for (Map<String, Object> role : result.getData()) {
                    Map<String, Object> v = new LinkedHashMap<>();
                    v.put("name", "role_" + role.get("roleCode"));
                    v.put("label", role.get("roleName"));
                    v.put("type", "role");
                    vars.add(v);
                }
                return ok(vars);
            }
        } catch (Exception e) {
            log.warn("获取角色变量失败: error={}", e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @GetMapping("/getDeptVariables")
    public Map<String, Object> getDeptVariables() {
        try {
            var result = systemFeignClient.getDeptTree();
            if (result.isSuccess() && result.getData() != null) {
                List<Map<String, Object>> vars = new ArrayList<>();
                collectDeptVars(result.getData(), vars);
                return ok(vars);
            }
        } catch (Exception e) {
            log.warn("获取部门变量失败: error={}", e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private void collectDeptVars(List<Map<String, Object>> depts, List<Map<String, Object>> vars) {
        for (Map<String, Object> dept : depts) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("name", "dept_" + dept.get("id"));
            v.put("label", dept.get("deptName"));
            v.put("type", "department");
            vars.add(v);
            Object children = dept.get("children");
            if (children instanceof List<?> list && !list.isEmpty()) {
                collectDeptVars((List<Map<String, Object>>) list, vars);
            }
        }
    }

    @GetMapping("/getBaseVariables")
    public Map<String, Object> getBaseVariables() {
        List<Map<String, Object>> vars = new ArrayList<>();
        vars.add(variable("initiator", "流程发起人", "user"));
        vars.add(variable("initiatorName", "发起人姓名", "string"));
        vars.add(variable("initiatorDept", "发起人部门", "department"));
        vars.add(variable("businessKey", "业务Key", "string"));
        vars.add(variable("processInstanceId", "流程实例ID", "string"));
        vars.add(variable("startTime", "流程启动时间", "datetime"));
        vars.add(variable("currentTime", "当前时间", "datetime"));
        return ok(vars);
    }

    @GetMapping("/getCompanyVariables")
    public Map<String, Object> getCompanyVariables() {
        // 公司信息暂从系统配置获取
        return ok(Collections.emptyList());
    }

    @GetMapping("/getMatrixDeptVariables")
    public Map<String, Object> getMatrixDeptVariables() { return ok(Collections.emptyList()); }

    @GetMapping("/getMatrixCompanyVariables")
    public Map<String, Object> getMatrixCompanyVariables() { return ok(Collections.emptyList()); }

    @PostMapping("/getMatrixRoles/{roleType}")
    public Map<String, Object> getMatrixRoles(@PathVariable Integer roleType) { return ok(Collections.emptyList()); }

    @GetMapping("/getTaskFormInfoByModelKey/{modelKey}")
    public Map<String, Object> getTaskFormInfoByModelKey(@PathVariable String modelKey) {
        try {
            ModelInfo info = modelInfoService.getByModelKey(modelKey);
            if (info != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("modelKey", info.getModelKey());
                data.put("modelName", info.getName());
                data.put("formType", info.getFormType());
                data.put("categoryCode", info.getCategoryCode());
                return ok(data);
            }
        } catch (Exception e) {
            log.warn("获取任务表单信息失败: modelKey={}, error={}", modelKey, e.getMessage());
        }
        return ok(Collections.emptyMap());
    }

    // ==================== 流程追踪 ====================

    @GetMapping("/getOneActivityVoByProcessInstanceIdAndActivityId/{instanceId}/{activityId}")
    public Map<String, Object> getOneActivityVo(@PathVariable String instanceId, @PathVariable String activityId) {
        try {
            ActivityVo vo = flowProcessDiagramService.getOneActivityVoByProcessInstanceIdAndActivityId(instanceId, activityId);
            return ok(vo != null ? vo : Collections.emptyMap());
        } catch (Exception e) {
            log.warn("查询活动节点详情失败: instanceId={}, activityId={}, error={}", instanceId, activityId, e.getMessage());
            return ok(Collections.emptyMap());
        }
    }

    @GetMapping("/getHighLightedNodeVoByProcessInstanceId/{instanceId}")
    public Map<String, Object> getHighLightedNodeVo(@PathVariable String instanceId) {
        try {
            HighLightedNodeVo vo = flowProcessDiagramService.getHighLightedNodeVoByProcessInstanceId(instanceId);
            return ok(vo != null ? vo : Collections.emptyMap());
        } catch (Exception e) {
            log.warn("查询高亮节点失败: instanceId={}, error={}", instanceId, e.getMessage());
            return ok(Collections.emptyMap());
        }
    }

    // ==================== DMN (暂存) ====================

    @PostMapping("/getDmnPagerModel")
    public Map<String, Object> getDmnPagerModel(@RequestBody Map<String, Object> params) { return ok(Collections.emptyMap()); }

    @GetMapping("/getDmnByModelId/{modelId}")
    public Map<String, Object> getDmnByModelId(@PathVariable String modelId) { return ok(Collections.emptyMap()); }

    @GetMapping("/getDmnByModelKey/{modelKey}")
    public Map<String, Object> getDmnByModelKey(@PathVariable String modelKey) { return ok(Collections.emptyMap()); }

    // ==================== 表单选择 ====================

    @SuppressWarnings("unchecked")
    @PostMapping("/getCustomFormPagerModel")
    public Map<String, Object> getCustomFormPagerModel(@RequestBody Map<String, Object> params) {
        Map<String, Object> query = getMapParam(params, "query");
        int pageNum = getIntParam(query, "pageNum", 1);
        int pageSize = getIntParam(query, "pageSize", 20);

        Map<String, Object> data = new LinkedHashMap<>();
        try {
            var result = formFeignClient.getDefinitionPage(pageNum, pageSize);
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> pageData = result.getData();
                List<Map<String, Object>> records = (List<Map<String, Object>>) pageData.getOrDefault("records", List.of());
                List<Map<String, Object>> rows = records.stream()
                        .filter(d -> !"ARCHIVED".equals(d.get("status")))
                        .map(d -> {
                            Map<String, Object> f = new LinkedHashMap<>();
                            f.put("id", d.get("id"));
                            f.put("code", d.get("code"));
                            f.put("name", d.get("name"));
                            f.put("title", d.get("name"));
                            f.put("categoryCode", d.get("categoryCode"));
                            f.put("categoryName", d.get("categoryName"));
                            return f;
                        }).toList();
                data.put("rows", rows);
                data.put("total", pageData.getOrDefault("total", rows.size()));
                return ok(data);
            }
        } catch (Exception e) {
            log.warn("获取自定义表单列表失败: {}", e.getMessage());
        }
        data.put("rows", Collections.emptyList());
        data.put("total", 0);
        return ok(data);
    }

    // ==================== 模型发布/停用 ====================

    @PostMapping("/publishBpmn/{modelId}")
    public Map<String, Object> publishBpmn(@PathVariable String modelId) {
        Result<String> result = flowableBpmnService.publishBpmn(modelId);
        if (result.isSuccess()) {
            Map<String, Object> r = ok(result.getData());
            r.put("success", true);
            return r;
        }
        Map<String, Object> r = fail(result.getMessage());
        r.put("success", false);
        return r;
    }

    @PostMapping("/stopBpmn/{modelId}")
    public Map<String, Object> stopBpmn(@PathVariable String modelId) {
        Result<String> result = flowableBpmnService.stopBpmn(modelId);
        if (result.isSuccess()) {
            Map<String, Object> r = ok(result.getData());
            r.put("success", true);
            return r;
        }
        Map<String, Object> r = fail(result.getMessage());
        r.put("success", false);
        return r;
    }

    // ==================== 流程追踪补充 ====================

    @GetMapping("/getProcessActivityVosByProcessInstanceId/{instanceId}")
    public Map<String, Object> getProcessActivityVosByProcessInstanceId(@PathVariable String instanceId) {
        try {
            var list = flowProcessDiagramService.getProcessActivityVosByProcessInstanceId(instanceId);
            return ok(list != null ? list : Collections.emptyList());
        } catch (Exception e) {
            log.warn("查询流程活动节点列表失败: instanceId={}, error={}", instanceId, e.getMessage());
            return ok(Collections.emptyList());
        }
    }

    // ==================== 缺失端点补齐 ====================

    @GetMapping("/getPersonalVariables")
    public Map<String, Object> getPersonalVariables() {
        List<Map<String, Object>> vars = new ArrayList<>();
        vars.add(variable("userId", "用户ID", "string"));
        vars.add(variable("userName", "用户姓名", "string"));
        vars.add(variable("userDept", "用户部门", "department"));
        vars.add(variable("userRoles", "用户角色", "list"));
        return ok(vars);
    }

    @GetMapping("/getSequenceFlowVariableVos")
    public Map<String, Object> getSequenceFlowVariableVos() {
        List<Map<String, Object>> vars = new ArrayList<>();
        vars.add(variable("approved", "审批结果", "boolean"));
        vars.add(variable("rejected", "驳回", "boolean"));
        vars.add(variable("action", "审批动作", "string"));
        return ok(vars);
    }

    @GetMapping("/getPlatformType")
    public Map<String, Object> getPlatformType() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("success", true);
        data.put("data", "web");
        return data;
    }

    @GetMapping("/getProcessNameBaseInfos")
    public Map<String, Object> getProcessNameBaseInfos() {
        try {
            var definitions = repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .orderByProcessDefinitionName().asc()
                    .list();
            List<Map<String, Object>> infos = new ArrayList<>();
            for (var def : definitions) {
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("processKey", def.getKey());
                info.put("processName", def.getName());
                info.put("version", def.getVersion());
                info.put("deploymentId", def.getDeploymentId());
                infos.add(info);
            }
            return ok(infos);
        } catch (Exception e) {
            log.warn("获取流程定义列表失败: error={}", e.getMessage());
        }
        return ok(Collections.emptyList());
    }

    @PostMapping("/saveProcessNameExp")
    public Map<String, Object> saveProcessNameExp(@RequestBody Map<String, Object> params) {
        log.info("保存流程名称表达式: {}", params);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", SUCCESS);
        result.put("success", true);
        return result;
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

    private Map<String, Object> func(String name, String label, String desc, List<Map<String, Object>> args) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("name", name); f.put("label", label); f.put("description", desc); f.put("arguments", args);
        return f;
    }

    private Map<String, Object> arg(String name, String desc) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("name", name); a.put("description", desc);
        return a;
    }

    private Map<String, Object> listener(String event, String name, List<Map<String, Object>> params) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("listenerEvent", event); l.put("listenerName", name); l.put("params", params);
        return l;
    }

    private Map<String, Object> param(String name, String label, String placeholder) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("name", name); p.put("label", label); p.put("placeholder", placeholder);
        return p;
    }

    private Map<String, Object> variable(String name, String label, String type) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("name", name); v.put("label", label); v.put("type", type);
        return v;
    }

    private Map<String, String> cat(String code, String name) {
        Map<String, String> c = new LinkedHashMap<>();
        c.put("id", code); c.put("code", code); c.put("name", name);
        return c;
    }
}
