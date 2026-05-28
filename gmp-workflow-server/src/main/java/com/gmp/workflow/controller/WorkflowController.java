package com.gmp.workflow.controller;

import com.alibaba.fastjson2.JSON;
import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.framework.workflow.WorkflowService;
import com.gmp.workflow.constant.FlowConstant;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.service.flowable.*;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.vo.flowable.model.ModelInfoVo;
import com.gmp.workflow.vo.flowable.processinstance.StartorBaseInfoVo;
import com.gmp.workflow.vo.flowable.runtime.StartProcessInstanceVo;
import com.gmp.workflow.vo.flowable.task.CompleteTaskVo;
import com.gmp.workflow.vo.flowable.task.TaskQueryParamsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import org.flowable.engine.RuntimeService;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 流程管理控制器 — 基于 Flowable BPMN 2.0 + WorkflowService
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final IFlowableTaskService flowableTaskService;
    private final WorkflowService workflowService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final IExtendHisprocinstService extendHisprocinstService;
    private final IModelInfoService modelInfoService;
    private final CacheManager cacheManager;
    private final RuntimeService runtimeService;

    // ==================== 流程定义 ====================

    @GetMapping("/definition/page")
    public Result<Map<String, Object>> queryDefinitionPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        var modelQuery = repositoryService.createModelQuery()
                .orderByLastUpdateTime().desc();
        if (kw != null) modelQuery.modelNameLike("%" + kw + "%");
        long total = modelQuery.count();
        List<org.flowable.engine.repository.Model> models = modelQuery
                .listPage((pageNum - 1) * pageSize, pageSize);

        List<Map<String, Object>> records = models.stream().map(m -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", m.getId());
            r.put("processName", m.getName());
            r.put("processKey", m.getKey());
            r.put("version", m.getVersion());
            r.put("category", m.getCategory());
            r.put("status", "DRAFT");
            r.put("createTime", m.getCreateTime());
            r.put("lastUpdateTime", m.getLastUpdateTime());
            r.put("formKey", "");
            return r;
        }).toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.ok(data);
    }

    @DeleteMapping("/definition/{id}")
    public Result<Void> deleteDefinition(@PathVariable String id) {
        repositoryService.deleteModel(id);
        return Result.okMsg("删除成功");
    }

    @PutMapping("/definition/{id}/suspend")
    public Result<Void> suspendDefinition(@PathVariable String id) {
        String processDefId = resolveProcessDefinitionId(id);
        if (processDefId == null) return Result.fail(ResultCode.NOT_FOUND, "流程定义不存在");
        workflowService.suspendProcessDefinition(processDefId);
        return Result.okMsg("挂起成功");
    }

    @PutMapping("/definition/{id}/activate")
    public Result<Void> activateDefinition(@PathVariable String id) {
        String processDefId = resolveProcessDefinitionId(id);
        if (processDefId == null) return Result.fail(ResultCode.NOT_FOUND, "流程定义不存在");
        workflowService.activateProcessDefinition(processDefId);
        return Result.okMsg("激活成功");
    }

    /** 将 Model ID 转换为 ProcessDefinition ID */
    private String resolveProcessDefinitionId(String modelOrDefId) {
        // 先尝试直接作为 ProcessDefinition ID
        var pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(modelOrDefId).singleResult();
        if (pd != null) return modelOrDefId;

        // 否则当作 Model ID，通过 Model key 查找最新 ProcessDefinition
        var model = repositoryService.getModel(modelOrDefId);
        if (model == null) return null;
        var latestPd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(model.getKey()).latestVersion().singleResult();
        return latestPd != null ? latestPd.getId() : null;
    }

    // ==================== 流程实例 ====================

    @PostMapping("/instance/start")
    public Result<Map<String, Object>> startProcess(@RequestBody StartProcessInstanceVo vo) {
        // 重复提交防护：通过缓存防止短时间内重复发起相同流程
        org.springframework.cache.Cache startCache = cacheManager.getCache(FlowConstant.CACHE_START_PROCESSINSTANCE);
        if (startCache != null) {
            String cacheKey = vo.getProcessDefinitionKey() + ":" + vo.getBusinessKey() + ":" + vo.getCurrentUserCode();
            if (startCache.get(cacheKey) != null) {
                return Result.fail(ResultCode.WORKFLOW_ERROR, "请勿重复提交流程");
            }
            startCache.put(cacheKey, 1);
        }

        Map<String, Object> variables = vo.getVariables() != null ? vo.getVariables() : new HashMap<>();
        ProcessInstance pi = workflowService.startProcess(
                vo.getProcessDefinitionKey(),
                vo.getBusinessKey(),
                variables,
                vo.getCurrentUserCode());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processInstanceId", pi.getId());
        result.put("businessKey", pi.getBusinessKey());
        result.put("processStatus", "APPROVING");
        return Result.ok("流程启动成功", result);
    }

    @GetMapping("/instance/my-started")
    public Result<Map<String, Object>> queryMyStarted(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("X-User-Name") String username) {
        var query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(username)
                .orderByProcessInstanceStartTime().desc();
        long total = query.count();
        List<HistoricProcessInstance> list = query.listPage((pageNum - 1) * pageSize, pageSize);
        List<Map<String, Object>> records = list.stream().map(pi -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("processInstanceId", pi.getId());
            m.put("name", pi.getName());
            m.put("businessKey", pi.getBusinessKey());
            m.put("startUserId", pi.getStartUserId());
            m.put("startTime", pi.getStartTime());
            m.put("endTime", pi.getEndTime());
            return m;
        }).toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        return Result.ok(data);
    }

    @GetMapping("/instance/{id}")
    public Result<Map<String, Object>> getInstanceDetail(@PathVariable String id) {
        var pi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(id).singleResult();
        if (pi == null) return Result.fail(ResultCode.NOT_FOUND, "流程实例不存在");
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", pi.getId());
        detail.put("name", pi.getName());
        detail.put("businessKey", pi.getBusinessKey());
        detail.put("startUserId", pi.getStartUserId());
        detail.put("startTime", pi.getStartTime());
        detail.put("endTime", pi.getEndTime());
        return Result.ok(detail);
    }

    /**
     * 获取流程发起人基础信息
     */
    @GetMapping("/instance/{id}/starter-info")
    public Result<StartorBaseInfoVo> getStarterInfo(@PathVariable String id) {
        try {
            ExtendHisprocinst extendHisprocinst = extendHisprocinstService.findExtendHisprocinstByProcessInstanceId(id);
            if (extendHisprocinst == null) {
                return Result.fail(ResultCode.NOT_FOUND, "未找到流程实例扩展信息");
            }
            StartorBaseInfoVo vo = new StartorBaseInfoVo();
            vo.setProcessInstanceId(id);
            vo.setBusinessKey(extendHisprocinst.getBusinessKey());
            vo.setFormName(extendHisprocinst.getProcessName());
            vo.setModelKey(extendHisprocinst.getModelKey());
            // 查询模型名称
            if (StringUtils.isNotBlank(extendHisprocinst.getModelKey())) {
                var modelInfo = modelInfoService.getByModelKey(extendHisprocinst.getModelKey());
                if (modelInfo != null) {
                    vo.setModelName(modelInfo.getName());
                }
            }
            // 解析发起人信息
            if (StringUtils.isNotBlank(extendHisprocinst.getUserInfo())) {
                try {
                    vo.setStarterInfo(JSON.parseObject(extendHisprocinst.getUserInfo()));
                } catch (Exception ignored) {
                }
            }
            vo.setCreateTime(extendHisprocinst.getCreateTime());
            return Result.ok(vo);
        } catch (Exception e) {
            log.error("获取流程发起人信息失败: {}", id, e);
            return Result.fail(ResultCode.WORKFLOW_ERROR, e.getMessage());
        }
    }

    @PutMapping("/instance/{id}/terminate")
    public Result<Void> terminateProcess(@PathVariable String id,
                                         @RequestBody Map<String, String> body) {
        workflowService.terminateProcess(id, body.getOrDefault("reason", "手动终止"));
        return Result.okMsg("终止成功");
    }

    // ==================== 任务管理 ====================

    @GetMapping("/task/todo")
    public Result<Map<String, Object>> queryTodoTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("X-User-Name") String username,
            @RequestParam(required = false) String keyword) {
        TaskQueryParamsVo params = new TaskQueryParamsVo();
        params.setUserCode(username);
        params.setKeyword(keyword);
        var pager = flowableTaskService.getAppingTasksPagerModel(params,
                new Query() {{ setPageNum(pageNum); setPageSize(pageSize); }});
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", pager.getRows());
        data.put("total", pager.getTotal());
        return Result.ok(data);
    }

    @GetMapping("/task/done")
    public Result<Map<String, Object>> queryDoneTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("X-User-Name") String username,
            @RequestParam(required = false) String keyword) {
        TaskQueryParamsVo params = new TaskQueryParamsVo();
        params.setUserCode(username);
        params.setKeyword(keyword);
        var pager = flowableTaskService.getApplyedTasksPagerModel(params,
                new Query() {{ setPageNum(pageNum); setPageSize(pageSize); }});
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", pager.getRows());
        data.put("total", pager.getTotal());
        return Result.ok(data);
    }

    @GetMapping("/task/claimable")
    public Result<List<Map<String, Object>>> queryClaimableTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateOrAssigned(null)
                .orderByTaskCreateTime().asc()
                .listPage((pageNum - 1) * pageSize, pageSize);
        List<Map<String, Object>> records = tasks.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("taskId", t.getId());
            m.put("name", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("createTime", t.getCreateTime());
            return m;
        }).toList();
        return Result.ok(records);
    }

    @PostMapping("/task/{taskId}/claim")
    public Result<Void> claimTask(@PathVariable String taskId,
                                  @RequestBody Map<String, String> body) {
        workflowService.claimTask(taskId, body.get("userId"));
        return Result.okMsg("认领成功");
    }

    @PostMapping("/task/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable String taskId,
                                     @RequestBody CompleteTaskVo vo) {
        vo.setTaskId(taskId);
        try {
            flowableTaskService.complete(vo);
        } catch (Exception e) {
            log.error("办理任务失败", e);
            return Result.fail(ResultCode.WORKFLOW_ERROR, e.getMessage());
        }
        return Result.okMsg("办理成功");
    }

    @PostMapping("/task/{taskId}/delegate")
    public Result<Void> delegateTask(@PathVariable String taskId,
                                     @RequestBody Map<String, String> body) {
        workflowService.delegateTask(taskId, body.get("delegateUserId"), body.get("assignee"));
        return Result.okMsg("委派成功");
    }

    @PostMapping("/task/{taskId}/transfer")
    public Result<Void> transferTask(@PathVariable String taskId,
                                     @RequestBody Map<String, String> body) {
        workflowService.transferTask(taskId, body.get("transferUserId"), body.get("assignee"));
        return Result.okMsg("转办成功");
    }

    // ==================== 版本管理 ====================

    @GetMapping("/definition/{key}/versions")
    public Result<List<Map<String, Object>>> getDefinitionVersions(@PathVariable String key) {
        var definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc()
                .list();
        List<Map<String, Object>> versions = definitions.stream().map(d -> {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", d.getId());
            v.put("version", d.getVersion());
            v.put("name", d.getName());
            v.put("deploymentId", d.getDeploymentId());
            v.put("suspended", d.isSuspended());
            return v;
        }).toList();
        return Result.ok(versions);
    }

    /**
     * 回滚流程定义 — 重新部署指定版本
     */
    @PostMapping("/definition/{id}/rollback")
    public Result<Void> rollbackDefinition(@PathVariable String id,
                                           @RequestParam(defaultValue = "1") int targetVersion) {
        // 通过流程定义ID获取key，再查找目标版本
        var currentPd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        if (currentPd == null) return Result.fail(ResultCode.NOT_FOUND, "流程定义不存在");

        var targetPd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(currentPd.getKey())
                .processDefinitionVersion(targetVersion)
                .singleResult();
        if (targetPd == null) return Result.fail(ResultCode.NOT_FOUND, "目标版本不存在");

        // 从目标版本的部署中获取BPMN XML并重新部署
        var deployment = repositoryService.createDeploymentQuery()
                .deploymentId(targetPd.getDeploymentId()).singleResult();
        if (deployment == null) return Result.fail(ResultCode.NOT_FOUND, "部署记录不存在");

        var resources = repositoryService.getDeploymentResourceNames(targetPd.getDeploymentId());
        if (resources == null || resources.isEmpty())
            return Result.fail(ResultCode.WORKFLOW_ERROR, "部署资源不存在");

        String bpmnXml = null;
        for (String resName : resources) {
            if (resName.endsWith(".bpmn20.xml") || resName.endsWith(".bpmn")) {
                try (var resource = repositoryService.getResourceAsStream(targetPd.getDeploymentId(), resName)) {
                    if (resource != null) {
                        bpmnXml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
                        break;
                    }
                } catch (java.io.IOException e) {
                    log.error("读取BPMN资源失败: deploymentId={}, res={}", targetPd.getDeploymentId(), resName, e);
                }
            }
        }
        if (bpmnXml == null) return Result.fail(ResultCode.WORKFLOW_ERROR, "无法读取BPMN资源");

        repositoryService.createDeployment()
                .addString(currentPd.getKey() + ".bpmn20.xml", bpmnXml)
                .name(currentPd.getName() + " (回滚自v" + targetVersion + ")")
                .category(currentPd.getCategory())
                .deploy();
        log.info("流程定义已回滚: key={}, from=v{}", currentPd.getKey(), targetVersion);
        return Result.okMsg("已回滚到版本 " + targetVersion);
    }

    // ==================== 撤回 / 加签 / 催办 ====================

    /**
     * 撤回任务 — 发起人将已提交但未完成的任务撤回到自己手中
     */
    @PostMapping("/task/{taskId}/withdraw")
    public Result<Void> withdrawTask(@PathVariable String taskId,
                                     @RequestBody(required = false) Map<String, String> body) {
        workflowService.withdrawTask(taskId, body != null ? body.get("reason") : null);
        return Result.okMsg("撤回成功");
    }

    /**
     * 加签 — 在当前审批节点增加审批人
     * type: QJQ(前加签) / HJQ(后加签)
     */
    @PostMapping("/task/{taskId}/countersign")
    public Result<Void> countersignTask(@PathVariable String taskId,
                                        @RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "HJQ");
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) body.get("userIds");
        String reason = (String) body.get("reason");
        if (userIds == null || userIds.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "加签人列表不能为空");
        }
        workflowService.countersignTask(taskId, type, userIds, reason);
        return Result.okMsg("加签成功");
    }

    /**
     * 催办 — 向当前审批人发送提醒
     */
    @PostMapping("/task/{taskId}/urge")
    public Result<Void> urgeTask(@PathVariable String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return Result.fail(ResultCode.NOT_FOUND, "任务不存在");
        // 查询当前待办任务的审批人
        String assignee = task.getAssignee();
        if (assignee == null || assignee.isEmpty()) {
            return Result.fail(ResultCode.WORKFLOW_ERROR, "当前任务无审批人，无法催办");
        }
        workflowService.urgeTask(taskId);
        return Result.okMsg("催办已发送");
    }

    // ==================== 历史 ====================

    @GetMapping("/history/trace/{instanceId}")
    public Result<List<Map<String, Object>>> queryProcessTrace(@PathVariable String instanceId) {
        var activities = workflowService.queryProcessTrace(instanceId);
        List<Map<String, Object>> trace = activities.stream().map(a -> {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("activityId", a.getActivityId());
            node.put("activityName", a.getActivityName());
            node.put("activityType", a.getActivityType());
            node.put("assignee", a.getAssignee());
            node.put("startTime", a.getStartTime());
            node.put("endTime", a.getEndTime());
            node.put("durationInMillis", a.getDurationInMillis());
            return node;
        }).toList();
        return Result.ok(trace);
    }

    @GetMapping("/task/todo-count")
    public Result<Map<String, Long>> getTodoCount(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestHeader("X-User-Name") String username) {
        TaskQueryParamsVo params = new TaskQueryParamsVo();
        params.setUserCode(username);
        long todo = flowableTaskService.getAppingTaskCont(params);
        long claimable = taskService.createTaskQuery().taskUnassigned().count();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("todo", todo);
        counts.put("claimable", claimable);
        return Result.ok(counts);
    }
}
