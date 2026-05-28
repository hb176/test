package com.gmp.workflow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时自动部署默认流程定义
 * <p>
 * 检查 DMS_DOCUMENT / TMS_PLAN / TMS_COURSE 是否已部署，
 * 未部署则编程式创建简单审批流程并部署。
 * </p>
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class DefaultProcessDeployer implements ApplicationRunner {

    private final RepositoryService repositoryService;

    @Override
    public void run(ApplicationArguments args) {
        deployIfAbsent("DMS_DOCUMENT", "文件审批流程");
        deployIfAbsent("TMS_PLAN", "培训计划审批流程");
        deployIfAbsent("TMS_COURSE", "培训课程审批流程");
    }

    private void deployIfAbsent(String processKey, String processName) {
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey).latestVersion().count();
        if (count > 0) {
            log.debug("流程定义已存在，跳过部署: key={}", processKey);
            return;
        }

        BpmnModel model = buildApprovalProcess(processKey, processName);
        String xml = new String(repositoryService.createDeployment()
                .addBpmnModel(processKey + ".bpmn20.xml", model)
                .name(processName)
                .category("SYSTEM")
                .deploy()
                .getId().getBytes());

        log.info("默认流程定义已部署: key={}, name={}", processKey, processName);
    }

    /**
     * 构建简单审批流程：开始 → 用户审核 → 排他网关 → 通过/驳回 → 结束
     */
    private BpmnModel buildApprovalProcess(String processKey, String processName) {
        BpmnModel model = new BpmnModel();
        org.flowable.bpmn.model.Process process = new org.flowable.bpmn.model.Process();
        process.setId(processKey);
        process.setName(processName);
        model.addProcess(process);

        // 开始节点
        StartEvent start = new StartEvent();
        start.setId("start");
        process.addFlowElement(start);

        // 审批用户任务
        UserTask approveTask = new UserTask();
        approveTask.setId("approveTask");
        approveTask.setName("审核");
        approveTask.setAssignee("${initiatorName}");
        process.addFlowElement(approveTask);

        // 排他网关
        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId("decision");
        process.addFlowElement(gateway);

        // 通过结束节点
        EndEvent endApproved = new EndEvent();
        endApproved.setId("endApproved");
        process.addFlowElement(endApproved);

        // 驳回结束节点
        EndEvent endRejected = new EndEvent();
        endRejected.setId("endRejected");
        process.addFlowElement(endRejected);

        // 连线
        process.addFlowElement(createFlow("start", "approveTask", "start", null));
        process.addFlowElement(createFlow("approveTask", "decision", "toDecision", null));
        process.addFlowElement(createFlow("decision", "endApproved", "approved", "${approved}"));
        process.addFlowElement(createFlow("decision", "endRejected", "rejected", "${!approved}"));

        return model;
    }

    private SequenceFlow createFlow(String from, String to, String id, String condition) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        if (condition != null) {
            flow.setConditionExpression(condition);
        }
        return flow;
    }
}
