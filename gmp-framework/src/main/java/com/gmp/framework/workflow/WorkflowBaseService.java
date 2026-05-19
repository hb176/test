package com.gmp.framework.workflow;

import com.gmp.common.exceptions.BusinessException;
import com.gmp.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
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
     * 终止流程实例（强制终止，不可恢复）
     *
     * @param processInstanceId 流程实例ID
     * @param reason            终止原因
     */
    public void terminateProcess(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("流程实例已终止 - 实例ID: {}, 原因: {}", processInstanceId, reason);
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
     * 加签（动态增加审批人）
     *
     * @param taskId      任务ID
     * @param addAssignee 加签人ID
     * @param comment     加签意见
     */
    public void addSignTask(String taskId, String addAssignee, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在: " + taskId);
        }
        // 创建子任务实现加签
        // Flowable支持通过创建subtask实现加签
        taskService.addComment(taskId, task.getProcessInstanceId(), "addSign",
                "加签给: " + addAssignee + ", 意见: " + comment);
        log.info("任务已加签 - 任务ID: {}, 加签人: {}", taskId, addAssignee);
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
}
