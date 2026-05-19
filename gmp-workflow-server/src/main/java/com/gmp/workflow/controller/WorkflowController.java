package com.gmp.workflow.controller;

import com.gmp.framework.base.Result;
import com.gmp.common.base.ResultCode;
import com.gmp.framework.workflow.WorkflowService;
import com.gmp.workflow.service.flowable.IFlowableTaskService;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.vo.flowable.processinstance.EndVo;
import com.gmp.workflow.vo.flowable.runtime.StartProcessInstanceVo;
import com.gmp.workflow.vo.flowable.task.CompleteTaskVo;
import com.gmp.workflow.vo.flowable.task.TaskQueryParamsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

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

    /** 删除模型 */
    @DeleteMapping("/definition/{id}")
    public Result<Void> deleteDefinition(@PathVariable String id) {
        repositoryService.deleteModel(id);
        return Result.okMsg("删除成功");
    }

    @PutMapping("/definition/{id}/suspend")
    public Result<Void> suspendDefinition(@PathVariable String id) {
        workflowService.suspendProcessDefinition(id);
        return Result.okMsg("挂起成功");
    }

    @PutMapping("/definition/{id}/activate")
    public Result<Void> activateDefinition(@PathVariable String id) {
        workflowService.activateProcessDefinition(id);
        return Result.okMsg("激活成功");
    }

    // ==================== 流程实例 ====================

    @PostMapping("/instance/start")
    public Result<Map<String, Object>> startProcess(@RequestBody StartProcessInstanceVo vo) {
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
        String userId = username;
        var query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
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
