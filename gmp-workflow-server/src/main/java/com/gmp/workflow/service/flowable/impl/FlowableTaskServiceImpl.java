package com.gmp.workflow.service.flowable.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.exception.FlowException;
import com.gmp.workflow.mapper.FlowableTaskMapper;
import com.gmp.workflow.service.flowable.BaseProcessService;
import com.gmp.workflow.service.flowable.ICommentInfoService;
import com.gmp.workflow.service.flowable.IExtendHisprocinstService;
import com.gmp.workflow.service.flowable.IFlowableTaskService;
import com.gmp.workflow.tools.common.ReturnCode;
import com.gmp.workflow.tools.common.UUIDGenerator;
import com.gmp.workflow.tools.pager.PagerModel;
import com.gmp.workflow.tools.pager.Query;
import com.gmp.workflow.tools.utils.DurationUtils;
import com.gmp.workflow.tools.vo.ReturnVo;
import com.gmp.workflow.vo.flowable.task.CompleteTaskVo;
import com.gmp.workflow.vo.flowable.task.TaskQueryParamsVo;
import com.gmp.workflow.vo.flowable.task.TaskVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.ManagementService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FlowableTaskServiceImpl extends BaseProcessService implements IFlowableTaskService {

    private final FlowableTaskMapper flowableTaskMapper;
    private final TaskService taskService;
    private final ManagementService managementService;

    public FlowableTaskServiceImpl(ICommentInfoService commentInfoService,
                                    IExtendHisprocinstService extendHisprocinstService,
                                    CacheManager cacheManager,
                                    FlowableTaskMapper flowableTaskMapper,
                                    TaskService taskService,
                                    ManagementService managementService) {
        super(commentInfoService, extendHisprocinstService, cacheManager);
        this.flowableTaskMapper = flowableTaskMapper;
        this.taskService = taskService;
        this.managementService = managementService;
    }

    @Override
    public Long getAppingTaskCont(TaskQueryParamsVo params) {
        return flowableTaskMapper.getAppingTaskCont(params);
    }

    @Override
    public PagerModel<TaskVo> getAppingTasksPagerModel(TaskQueryParamsVo params, Query query) {
        IPage<TaskVo> queryPage = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<TaskVo> page = flowableTaskMapper.getAppingTasksPagerModel(queryPage, params);
        enrichTaskVoList(page.getRecords());
        return new PagerModel<>(page.getTotal(), page.getRecords());
    }

    @Override
    public PagerModel<TaskVo> getApplyedTasksPagerModel(TaskQueryParamsVo params, Query query) {
        IPage<TaskVo> queryPage = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<TaskVo> page = flowableTaskMapper.getApplyedTasksPagerModel(queryPage, params);
        enrichTaskVoList(page.getRecords());
        return new PagerModel<>(page.getTotal(), page.getRecords());
    }

    @Override
    public ReturnVo<String> complete(CompleteTaskVo completeTaskVo) throws FlowException {
        ReturnVo<String> returnVo = new ReturnVo<>(ReturnCode.SUCCESS, "OK");
        try {
            if (completeTaskVo != null && StringUtils.isNotBlank(completeTaskVo.getTaskId())) {
                TaskEntity task = (TaskEntity) taskService.createTaskQuery()
                        .taskId(completeTaskVo.getTaskId()).singleResult();
                if (task != null) {
                    if (StringUtils.isBlank(completeTaskVo.getProcessInstanceId())) {
                        completeTaskVo.setProcessInstanceId(task.getProcessInstanceId());
                    }
                    evictHighLightedNodeCache(task.getProcessInstanceId());
                    evictOneActivityVoCache(task.getProcessInstanceId(), task.getTaskDefinitionKey());

                    String taskId = completeTaskVo.getTaskId();
                    if (DelegationState.PENDING.equals(task.getDelegationState())) {
                        Task subTask = createSubTask(task, task.getParentTaskId(), completeTaskVo.getUserCode());
                        taskService.complete(subTask.getId());
                        taskId = subTask.getId();
                        taskService.resolveTask(completeTaskVo.getTaskId(), completeTaskVo.getVariables());
                    } else {
                        flowableTaskMapper.updateHisAssignee(taskId, completeTaskVo.getUserCode());
                        if (MapUtils.isNotEmpty(completeTaskVo.getVariables())) {
                            taskService.complete(completeTaskVo.getTaskId(), completeTaskVo.getVariables());
                        } else {
                            taskService.complete(completeTaskVo.getTaskId());
                        }
                        String parentTaskId = task.getParentTaskId();
                        if (StringUtils.isNotBlank(parentTaskId)) {
                            String tableName = managementService.getTableName(TaskEntity.class);
                            String sql = "SELECT COUNT(1) FROM " + tableName
                                    + " WHERE PARENT_TASK_ID_=#{parentTaskId}";
                            long subTaskCount = taskService.createNativeTaskQuery()
                                    .sql(sql).parameter("parentTaskId", parentTaskId).count();
                            if (subTaskCount == 0) {
                                Task ptask = taskService.createTaskQuery().taskId(parentTaskId).singleResult();
                                taskService.resolveTask(parentTaskId);
                                if (CommentTypeEnum.HJQ.toString().equals(ptask.getScopeType())) {
                                    taskService.complete(parentTaskId);
                                }
                            }
                        }
                    }
                    completeTaskVo.setTaskId(taskId);
                    completeTaskVo.setActivityId(task.getTaskDefinitionKey());
                    completeTaskVo.setActivityName(task.getName());
                    addFlowCommentInfoAndProcessStatus(completeTaskVo);
                } else {
                    returnVo = new ReturnVo<>(ReturnCode.FAIL, "没有查询到任务!");
                }
            } else {
                returnVo = new ReturnVo<>(ReturnCode.FAIL, "taskId should not be null");
            }
        } catch (Exception e) {
            log.error("审批任务报错", e);
            throw new FlowException("审批任务报错", e);
        }
        return returnVo;
    }

    private Task createSubTask(TaskEntity ptask, String ptaskId, String assignee) {
        TaskEntity task = null;
        if (ptask != null) {
            task = (TaskEntity) taskService.newTask(UUIDGenerator.generate());
            task.setAssignee(assignee);
            task.setProcessInstanceId(ptask.getProcessInstanceId());
            task.setProcessDefinitionId(ptask.getProcessDefinitionId());
            task.setParentTaskId(ptaskId);
            task.setCategory(ptask.getCategory());
            task.setDescription(ptask.getDescription());
            task.setName(ptask.getName());
            task.setTaskDefinitionKey(ptask.getTaskDefinitionKey());
            task.setTaskDefinitionId(ptask.getTaskDefinitionId());
            task.setTenantId(ptask.getTenantId());
            task.setCreateTime(new Date());
            task.setPriority(ptask.getPriority());
            taskService.saveTask(task);
        }
        return task;
    }

    private void enrichTaskVoList(List<TaskVo> taskVoList) {
        if (CollectionUtils.isEmpty(taskVoList)) return;
        long currTime = System.currentTimeMillis();
        for (TaskVo vo : taskVoList) {
            vo.setProcessStatusName(ProcessStatusEnum.getEnumMsgByType(vo.getProcessStatus()));
            if (vo.getCreateTime() != null) {
                long duration = currTime - vo.getCreateTime().getTime();
                vo.setStayHour(DurationUtils.getDuration(duration));
            }
            if (vo.getStartTime() != null) {
                long end = vo.getFinishedTime() != null
                        ? vo.getFinishedTime().getTime() : currTime;
                vo.setTotalTime(DurationUtils.getDuration(end - vo.getStartTime().getTime()));
            }
        }
    }
}
