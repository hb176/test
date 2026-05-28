package com.gmp.workflow.listener;

import com.gmp.framework.workflow.ProcessSlaService;
import com.gmp.workflow.feign.SystemFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GlobalTaskCreateListener extends AbstractFlowableEngineEventListener {

    private final ProcessSlaService slaService;
    private final SystemFeignClient systemFeignClient;

    public GlobalTaskCreateListener(ProcessSlaService slaService, SystemFeignClient systemFeignClient) {
        this.slaService = slaService;
        this.systemFeignClient = systemFeignClient;
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        if (event.getEntity() instanceof Task task) {
            String processKey = task.getProcessDefinitionId();
            String nodeKey = task.getTaskDefinitionKey();
            slaService.onTaskCreated(task.getId(), processKey, nodeKey != null ? nodeKey : "unknown");
            log.debug("SLA计时开始: taskId={}, processKey={}, nodeKey={}", task.getId(), processKey, nodeKey);

            // 发送消息通知审批人
            notifyAssignee(task);
        }
    }

    private void notifyAssignee(Task task) {
        String assignee = task.getAssignee();
        if (assignee == null || assignee.isEmpty()) return;

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("templateCode", "HCP_WF_Task_New");
            request.put("receiverId", Long.parseLong(assignee));
            request.put("receiverName", assignee);
            Map<String, String> variables = new HashMap<>();
            variables.put("wf_task_name", task.getName() != null ? task.getName() : "待办任务");
            variables.put("businessTitle", task.getDescription() != null ? task.getDescription() : "");
            variables.put("wf_pd_name", task.getProcessDefinitionId());
            variables.put("wf_createrName", "系统");
            variables.put("wf_createrTime", task.getCreateTime() != null ? task.getCreateTime().toString() : "");
            request.put("variables", variables);
            request.put("businessType", "WORKFLOW");
            systemFeignClient.sendMessageByTemplate(request);
            log.debug("已发送新任务通知: taskId={}, assignee={}", task.getId(), assignee);
        } catch (Exception e) {
            log.warn("发送新任务通知失败: taskId={}, error={}", task.getId(), e.getMessage());
        }
    }
}
