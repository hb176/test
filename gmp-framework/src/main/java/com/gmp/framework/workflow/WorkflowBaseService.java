package com.gmp.framework.workflow;

import com.gmp.common.exceptions.BusinessException;
import com.gmp.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;

import java.util.*;

/**
 * 流程引擎基础服务 - 封装Flowable BPMN 2.0工作流引擎的常用操作
 *
 * 核心能力：
 * 1. 流程定义管理：部署、查询、挂起/激活
 * 2. 流程实例管理：启动、挂起、终止、跳转
 * 3. 任务管理：查询待办、认领、完成、委派、转办
 * 4. 历史查询：已办任务、流程历史轨迹
 * 5. 流程变量：运行时变量存取
 * 6. 审批意见：任务级别的审批意见管理
 *
 * 使用前提：需要注入Flowable的核心引擎（通过Spring Boot自动配置）
 * - RepositoryService: 流程定义和部署
 * - RuntimeService: 流程实例和变量
 * - TaskService: 任务操作
 * - HistoryService: 历史查询
 * - ManagementService: 引擎管理
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
public abstract class WorkflowBaseService {

    protected final RepositoryService repositoryService;
    protected final RuntimeService runtimeService;
    protected final TaskService taskService;
    protected final HistoryService historyService;
    protected final ManagementService managementService;

    protected WorkflowBaseService(RepositoryService repositoryService,
                                  RuntimeService runtimeService,
                                  TaskService taskService,
                                  HistoryService historyService,
                                  ManagementService managementService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.managementService = managementService;
    }

    // ==================== 流程定义管理 ====================

    /**
     * 根据流程定义Key查询最新版本的流程定义
     *
     * @param processDefinitionKey 流程定义的Key（BPMN XML中的process id）
     * @return 最新的流程定义，不存在返回null
     */
    public ProcessDefinition getLatestProcessDefinition(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();
    }

    /**
     * 查询所有已部署的流程定义列表
     *
     * @return 流程定义列表
     */
    public List<ProcessDefinition> listAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc()
                .list();
    }

    /**
     * 挂起流程定义（挂起后不能启动新实例）
     *
     * @param processDefinitionId 流程定义ID
     */
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
        log.info("流程定义已挂起: {}", processDefinitionId);
    }

    /**
     * 激活流程定义（恢复启动新实例的能力）
     *
     * @param processDefinitionId 流程定义ID
     */
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
        log.info("流程定义已激活: {}", processDefinitionId);
    }

    // ==================== 流程实例管理 ====================

    /**
     * 启动流程实例
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey          业务键（用于关联业务数据，如记录ID）
     * @param variables            流程初始变量
     * @param initiator            发起人ID
     * @return 流程实例
     * @throws BusinessException 流程定义不存在或启动失败
     */
    public ProcessInstance startProcess(String processDefinitionKey, String businessKey,
                                         Map<String, Object> variables, String initiator) {
        ProcessDefinition definition = getLatestProcessDefinition(processDefinitionKey);
        if (definition == null) {
            throw new BusinessException(ResultCode.WORKFLOW_ERROR,
                    "流程定义不存在: " + processDefinitionKey);
        }
        if (definition.isSuspended()) {
            throw new BusinessException(ResultCode.WORKFLOW_ERROR,
                    "流程定义已挂起，无法启动新实例: " + processDefinitionKey);
        }

        // 设置流程发起人
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("initiator", initiator);
        variables.put("businessKey", businessKey);
        variables.put("startTime", new Date());

        // 通过IdentityService设置当前操作人（Flowable需要）
        try {
            ProcessInstance instance = runtimeService.startProcessInstanceById(
                    definition.getId(), businessKey, variables);
            log.info("流程实例已启动 - 流程Key: {}, 业务键: {}, 实例ID: {}, 发起人: {}",
                    processDefinitionKey, businessKey, instance.getId(), initiator);
            return instance;
        } catch (Exception e) {
            log.error("启动流程实例失败 - 流程Key: {}, 业务键: {}", processDefinitionKey, businessKey, e);
            throw new BusinessException(ResultCode.WORKFLOW_ERROR, "启动流程失败: " + e.getMessage());
        }
    }

    /**
     * 挂起流程实例（暂停审批流转）
     *
     * @param processInstanceId 流程实例ID
     */
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
        log.info("流程实例已挂起: {}", processInstanceId);
    }

    /**
     * 激活流程实例（恢复审批流转）
     *
     * @param processInstanceId 流程实例ID
     */
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
        log.info("流程实例已激活: {}", processInstanceId);
    }

    /**
     * 终止流程实例
     * <p>
     * 将所有执行实例跳转到结束事件节点，保留完整的流程历史数据。
     * 与硬删除（deleteProcessInstance）不同，此方式会在 act_hi_actinst 中
     * 留下完整的执行轨迹，便于审计和流程追踪。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @param reason            终止原因
     */
    public void terminateProcess(String processInstanceId, String reason) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            log.warn("流程实例不存在，可能已结束: {}", processInstanceId);
            return;
        }

        // 查找结束事件节点
        String processDefinitionId = processInstance.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process mainProcess = bpmnModel.getMainProcess();
        List<EndEvent> endEvents = mainProcess.findFlowElementsOfType(EndEvent.class);
        if (endEvents == null || endEvents.isEmpty()) {
            // 找不到结束节点，降级为硬删除
            log.warn("BPMN 模型中未找到结束事件节点，降级为硬删除: {}", processInstanceId);
            runtimeService.deleteProcessInstance(processInstanceId, reason);
            return;
        }
        String endEventId = endEvents.get(0).getId();

        // 检查是否有子流程
        ProcessInstance subProcess = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).singleResult();

        List<String> executionIds = new ArrayList<>();
        if (subProcess != null) {
            // 存在子流程，查找子流程的结束事件
            BpmnModel subBpmnModel = repositoryService.getBpmnModel(subProcess.getProcessDefinitionId());
            Process subMainProcess = subBpmnModel.getMainProcess();
            List<EndEvent> subEndEvents = subMainProcess.findFlowElementsOfType(EndEvent.class);
            if (subEndEvents != null && !subEndEvents.isEmpty()) {
                endEventId = subEndEvents.get(0).getId();
            }
            List<Execution> executions = runtimeService.createExecutionQuery()
                    .parentId(subProcess.getProcessInstanceId()).list();
            executions.forEach(e -> executionIds.add(e.getId()));
        } else {
            List<Execution> executions = runtimeService.createExecutionQuery()
                    .parentId(processInstanceId).list();
            executions.forEach(e -> executionIds.add(e.getId()));
        }

        if (!executionIds.isEmpty()) {
            runtimeService.createChangeActivityStateBuilder()
                    .moveExecutionsToSingleActivityId(executionIds, endEventId)
                    .changeState();
        }
        log.info("流程实例已终止（跳转到结束节点）- 实例ID: {}, 原因: {}", processInstanceId, reason);
    }

    // ==================== 任务管理 ====================

    /**
     * 查询用户的待办任务列表
     *
     * @param assignee 办理人ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 待办任务列表
     */
    public List<Task> queryTodoTasks(String assignee, int pageNum, int pageSize) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc()
                .listPage((pageNum - 1) * pageSize, pageSize);
    }

    /**
     * 查询用户的待办任务总数
     *
     * @param assignee 办理人ID
     * @return 待办任务数量
     */
    public long countTodoTasks(String assignee) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .count();
    }

    /**
     * 查询候选组任务（组任务需要先认领再办理）
     *
     * @param candidateGroup 候选组ID（通常为部门ID或角色编码）
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @return 候选组任务列表
     */
    public List<Task> queryGroupTasks(String candidateGroup, int pageNum, int pageSize) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(candidateGroup)
                .orderByTaskCreateTime().desc()
                .listPage((pageNum - 1) * pageSize, pageSize);
    }

    /**
     * 认领任务（将组任务分配给个人）
     *
     * @param taskId  任务ID
     * @param userId  认领人ID
     * @throws BusinessException 任务已被他人认领
     */
    public void claimTask(String taskId, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        if (task.getAssignee() != null) {
            throw new BusinessException(ResultCode.WORKFLOW_ERROR,
                    "任务已被认领，当前办理人: " + task.getAssignee());
        }
        taskService.claim(taskId, userId);
        log.info("任务已认领 - 任务ID: {}, 办理人: {}", taskId, userId);
    }

    /**
     * 完成审批任务
     *
     * @param taskId     任务ID
     * @param variables  流程变量（审批意见、分支条件等）
     * @param comment    审批意见文本
     * @param assignee   办理人ID
     * @throws BusinessException 任务不存在或无权限
     */
    public void completeTask(String taskId, Map<String, Object> variables,
                              String comment, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        if (!assignee.equals(task.getAssignee())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权办理此任务");
        }

        // 添加审批意见（Flowable的Comment机制）
        if (comment != null && !comment.isBlank()) {
            taskService.addComment(taskId, task.getProcessInstanceId(), "approval", comment);
        }

        // 设置流程变量
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("lastApprover", assignee);
        variables.put("lastApprovalTime", new Date());

        taskService.complete(taskId, variables);
        log.info("任务已完成 - 任务ID: {}, 办理人: {}, 意见: {}", taskId, assignee, comment);
    }

    /**
     * 驳回任务（退回到上一节点）
     *
     * @param taskId   任务ID
     * @param reason   驳回原因
     * @param assignee 操作人ID
     */
    public void rejectTask(String taskId, String reason, String assignee) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("rejectReason", reason);
        variables.put("rejectBy", assignee);
        variables.put("rejectTime", new Date());
        completeTask(taskId, variables, "驳回: " + reason, assignee);
        log.info("任务已驳回 - 任务ID: {}, 操作人: {}, 原因: {}", taskId, assignee, reason);
    }

    /**
     * 委派任务（将任务委派给他人办理）
     *
     * @param taskId     任务ID
     * @param delegateUserId 被委派人ID
     * @param assignee   当前办理人ID
     */
    public void delegateTask(String taskId, String delegateUserId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        taskService.delegateTask(taskId, delegateUserId);
        log.info("任务已委派 - 任务ID: {} -> 委派人: {}", taskId, delegateUserId);
    }

    /**
     * 转办任务（将任务转移给他人）
     *
     * @param taskId        任务ID
     * @param transferUserId 接收人ID
     * @param assignee      当前办理人ID
     */
    public void transferTask(String taskId, String transferUserId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        taskService.setAssignee(taskId, transferUserId);
        log.info("任务已转办 - 任务ID: {} -> 接收人: {}", taskId, transferUserId);
    }

    /**
     * 撤回任务 — 将流程实例退回到发起节点
     */
    public void withdrawTask(String taskId, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (pi == null) {
            throw new BusinessException(ResultCode.WORKFLOW_ERROR, "流程已结束，无法撤回");
        }
        runtimeService.setVariable(task.getProcessInstanceId(), "withdrawReason",
                reason != null ? reason : "发起人撤回");
        runtimeService.setVariable(task.getProcessInstanceId(), "withdrawn", true);
        runtimeService.deleteProcessInstance(task.getProcessInstanceId(),
                "WITHDRAWN: " + (reason != null ? reason : "发起人撤回"));
        log.info("流程已撤回: taskId={}, instanceId={}, reason={}", taskId, task.getProcessInstanceId(), reason);
    }

    /**
     * 加签 — 在当前任务记录加签审批人
     * @param type  QJQ(前加签)/HJQ(后加签)
     */
    public void countersignTask(String taskId, String type, List<String> userIds, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        for (String userId : userIds) {
            // 通过Comment记录加签信息
            String comment = String.format("[%s] 加签审批人=%s, 原因=%s",
                    "QJQ".equals(type) ? "前加签" : "后加签",
                    userId,
                    reason != null ? reason : "无");
            taskService.addComment(taskId, task.getProcessInstanceId(), "countersign", comment);
            // 设置加签标记变量，供后续Flowable流转判断
            runtimeService.setVariable(task.getProcessInstanceId(),
                    "countersign_" + userId + "_type", type);
        }
        log.info("任务已加签: taskId={}, type={}, users={}", taskId, type, userIds);
    }

    /**
     * 催办 — 记录催办操作（通知由消息中心发送）
     */
    public void urgeTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        taskService.addComment(taskId, task.getProcessInstanceId(), "urge",
                "催办提醒 — 请尽快处理");
        log.info("催办已发送: taskId={}, assignee={}", taskId, task.getAssignee());
    }

    // ==================== 历史查询 ====================

    /**
     * 查询用户已办任务历史
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 历史流程实例列表
     */
    public List<HistoricProcessInstance> queryDoneProcesses(String userId, int pageNum, int pageSize) {
        return historyService.createHistoricProcessInstanceQuery()
                .involvedUser(userId)
                .finished()
                .orderByProcessInstanceEndTime().desc()
                .listPage((pageNum - 1) * pageSize, pageSize);
    }

    /**
     * 查询流程实例的历史轨迹
     *
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    public List<org.flowable.engine.history.HistoricActivityInstance> queryProcessTrace(
            String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
    }

    /**
     * 检查流程实例是否已结束
     *
     * @param processInstanceId 流程实例ID
     * @return true=已结束
     */
    public boolean isProcessEnded(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return instance == null; // 运行时表中不存在说明已结束
    }

    // ==================== 流程变量 ====================

    /**
     * 设置流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName      变量名
     * @param value             变量值
     */
    public void setProcessVariable(String processInstanceId, String variableName, Object value) {
        runtimeService.setVariable(processInstanceId, variableName, value);
    }

    /**
     * 获取流程变量
     *
     * @param processInstanceId 流程实例ID
     * @param variableName      变量名
     * @return 变量值，不存在返回null
     */
    public Object getProcessVariable(String processInstanceId, String variableName) {
        return runtimeService.getVariable(processInstanceId, variableName);
    }

    /**
     * 获取流程的所有变量
     *
     * @param processInstanceId 流程实例ID
     * @return 变量Map
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    // ==================== Facade 方法（仅返回 Map/List，不暴露 Flowable 类型） ====================

    /** 模型分页查询 */
    public Map<String, Object> getModelPage(int pageNum, int pageSize, String keyword) {
        var q = repositoryService.createModelQuery().orderByLastUpdateTime().desc();
        if (keyword != null && !keyword.isEmpty()) q.modelNameLike("%" + keyword + "%");
        long total = q.count();
        var models = q.listPage((pageNum - 1) * pageSize, pageSize);
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (var m : models) {
            Map<String, Object> r = new java.util.LinkedHashMap<>();
            r.put("id", m.getId());
            r.put("processName", m.getName());
            r.put("processKey", m.getKey());
            r.put("version", m.getVersion());
            r.put("category", m.getCategory());
            r.put("status", "DRAFT");
            r.put("createTime", m.getCreateTime());
            r.put("lastUpdateTime", m.getLastUpdateTime());
            records.add(r);
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        return data;
    }

    /** 删除模型 */
    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }

    /** 启动流程实例（返回实例信息 Map） */
    public Map<String, Object> startProcessInstance(String processDefinitionKey, String businessKey,
                                                     Map<String, Object> variables, String initiator) {
        ProcessInstance pi = startProcess(processDefinitionKey, businessKey, variables, initiator);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("processInstanceId", pi.getId());
        result.put("businessKey", pi.getBusinessKey());
        result.put("processStatus", "APPROVING");
        return result;
    }

    /** 我发起的流程分页 */
    public Map<String, Object> getMyStartedProcesses(String userId, int pageNum, int pageSize) {
        var q = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId).orderByProcessInstanceStartTime().desc();
        long total = q.count();
        var list = q.listPage((pageNum - 1) * pageSize, pageSize);
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (var pi : list) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("processInstanceId", pi.getId());
            m.put("name", pi.getName());
            m.put("businessKey", pi.getBusinessKey());
            m.put("startUserId", pi.getStartUserId());
            m.put("startTime", pi.getStartTime());
            m.put("endTime", pi.getEndTime());
            records.add(m);
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("records", records);
        data.put("total", total);
        return data;
    }

    /** 流程实例详情 */
    public Map<String, Object> getInstanceDetail(String instanceId) {
        var pi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (pi == null) return null;
        Map<String, Object> detail = new java.util.LinkedHashMap<>();
        detail.put("id", pi.getId());
        detail.put("name", pi.getName());
        detail.put("businessKey", pi.getBusinessKey());
        detail.put("startUserId", pi.getStartUserId());
        detail.put("startTime", pi.getStartTime());
        detail.put("endTime", pi.getEndTime());
        return detail;
    }

    /** 候选任务列表 */
    public List<Map<String, Object>> getClaimableTasks(int pageNum, int pageSize) {
        var tasks = taskService.createTaskQuery()
                .taskCandidateOrAssigned(null)
                .orderByTaskCreateTime().asc()
                .listPage((pageNum - 1) * pageSize, pageSize);
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (var t : tasks) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("taskId", t.getId());
            m.put("name", t.getName());
            m.put("processInstanceId", t.getProcessInstanceId());
            m.put("createTime", t.getCreateTime());
            records.add(m);
        }
        return records;
    }

    /** 候选任务数量 */
    public long countClaimableTasks() {
        return taskService.createTaskQuery().taskUnassigned().count();
    }

    /** 流程执行轨迹 */
    public List<Map<String, Object>> getProcessTrace(String processInstanceId) {
        var activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
        List<Map<String, Object>> trace = new java.util.ArrayList<>();
        for (var a : activities) {
            Map<String, Object> node = new java.util.LinkedHashMap<>();
            node.put("activityId", a.getActivityId());
            node.put("activityName", a.getActivityName());
            node.put("activityType", a.getActivityType());
            node.put("assignee", a.getAssignee());
            node.put("startTime", a.getStartTime());
            node.put("endTime", a.getEndTime());
            node.put("durationInMillis", a.getDurationInMillis());
            trace.add(node);
        }
        return trace;
    }

    // ==================== BPMN 模型 Facade（供 BpmnDesignerController 使用） ====================

    /** 获取模型详情 */
    public Map<String, Object> getModelById(String modelId) {
        var model = repositoryService.getModel(modelId);
        if (model == null) return null;
        return modelToMap(model);
    }

    /** 按 key 查询模型 */
    public Map<String, Object> getModelByKey(String modelKey) {
        var models = repositoryService.createModelQuery().modelKey(modelKey).list();
        if (models.isEmpty()) return null;
        return modelToMap(models.get(0));
    }

    /** 获取模型 BPMN XML */
    public String getModelSource(String modelId) {
        byte[] source = repositoryService.getModelEditorSource(modelId);
        return source != null ? new String(source, java.nio.charset.StandardCharsets.UTF_8) : "";
    }

    /** 创建或更新模型，返回模型 ID */
    public String saveModel(String modelId, String name, String key, String category, String xml) {
        var model = (modelId != null && !modelId.isEmpty())
                ? repositoryService.getModel(modelId) : null;
        if (model == null) model = repositoryService.newModel();
        model.setName(name);
        model.setKey(key != null && !key.isEmpty() ? key : model.getId());
        model.setCategory(category != null && !category.isEmpty() ? category : "COMM");
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(),
                xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return model.getId();
    }

    /** 部署流程定义，返回部署结果消息 */
    public String deployModel(String name, String category, String xml) {
        try {
            repositoryService.createDeployment()
                    .addString(name + ".bpmn20.xml", xml)
                    .name(name)
                    .category(category)
                    .deploy();
            return "流程已自动部署";
        } catch (Exception e) {
            log.warn("部署流程失败(模型已保存): {}", e.getMessage());
            return "但部署失败: " + e.getMessage();
        }
    }

    /** 校验 BPMN XML */
    public String validateBpmn(String xml) {
        try {
            repositoryService.createDeployment()
                    .addString("validate.bpmn", xml).deploy();
            return null; // null = 通过
        } catch (Exception e) {
            return "校验失败: " + e.getMessage();
        }
    }

    /** 查询所有模型列表 */
    public List<Map<String, Object>> listAllModels() {
        var models = repositoryService.createModelQuery()
                .orderByLastUpdateTime().desc().list();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var m : models) result.add(modelToMap(m));
        return result;
    }

    private Map<String, Object> modelToMap(Object modelObj) {
        // 使用反射避免直接引用 org.flowable.engine.repository.Model 类型
        try {
            var m = (org.flowable.engine.repository.Model) modelObj;
            Map<String, Object> r = new java.util.LinkedHashMap<>();
            r.put("id", m.getId());
            r.put("modelId", m.getId());
            r.put("key", m.getKey());
            r.put("name", m.getName());
            r.put("category", m.getCategory());
            r.put("version", m.getVersion());
            r.put("createTime", m.getCreateTime());
            r.put("lastUpdateTime", m.getLastUpdateTime());
            return r;
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }
}
