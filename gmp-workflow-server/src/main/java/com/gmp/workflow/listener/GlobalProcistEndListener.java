package com.gmp.workflow.listener;

import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.feign.BusinessFeignClient;
import com.gmp.workflow.feign.SystemFeignClient;
import com.gmp.workflow.model.flowable.CommentInfo;
import com.gmp.workflow.model.flowable.ExtendHisprocinst;
import com.gmp.workflow.service.flowable.ICommentInfoService;
import com.gmp.workflow.service.flowable.IExtendHisprocinstService;
import com.gmp.workflow.service.flowable.IExtendProcinstService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.HistoryService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程实例结束全局监听器
 * <p>
 * 当流程实例结束时，自动执行以下操作：
 * 1. 更新历史扩展表状态为 BJ（办结），除非是 ZZ（终止）状态
 * 2. 添加"审批完成"的评论记录
 * 3. 删除运行时扩展信息（tbl_flow_extend_procinst）
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalProcistEndListener extends AbstractFlowableEngineEventListener {

    private final IExtendProcinstService extendProcinstService;
    private final IExtendHisprocinstService extendHisprocinstService;
    private final HistoryService historyService;
    private final ICommentInfoService commentInfoService;
    private final SystemFeignClient systemFeignClient;
    private final BusinessFeignClient businessFeignClient;

    /**
     * 流程实例结束事件处理
     * <p>
     * 更新历史扩展表状态、添加审批完成评论、删除运行时扩展信息。
     * </p>
     *
     * @param event 流程实体事件
     */
    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        FlowableEntityEventImpl flowableEntityEvent = (FlowableEntityEventImpl) event;
        String processInstanceId = flowableEntityEvent.getProcessInstanceId();
        updateExtendInfoToHis(processInstanceId);
        updateBusinessRecordStatus(processInstanceId);
        notifyProcessInitiator(processInstanceId);
    }

    /**
     * 流程结束后更新扩展信息
     */
    private void updateExtendInfoToHis(String processInstanceId) {
        // 1. 更新历史扩展表状态
        ExtendHisprocinst extendHisprocinst = extendHisprocinstService
                .findExtendHisprocinstByProcessInstanceId(processInstanceId);

        if (extendHisprocinst != null
                && !ProcessStatusEnum.ZZ.toString().equals(extendHisprocinst.getProcessStatus())) {
            // 非终止状态，更新为办结
            ExtendHisprocinst update = new ExtendHisprocinst(processInstanceId, ProcessStatusEnum.BJ.toString());
            extendHisprocinstService.updateAllStatusByProcessInstanceId(update);
        }

        // 2. 处理流程一发起就结束的场景（历史实例可能还没来得及写入）
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (historicProcessInstance == null) {
            ExtendHisprocinst update = new ExtendHisprocinst(processInstanceId, ProcessStatusEnum.BJ.toString());
            extendHisprocinstService.updateAllStatusByProcessInstanceId(update);
        }

        // 3. 添加审批完成评论
        try {
            CommentInfo commentInfo = new CommentInfo();
            commentInfo.setProcessInstanceId(processInstanceId);
            commentInfo.setType(CommentTypeEnum.SPJS.name());
            commentInfo.setMessage(CommentTypeEnum.SPJS.getName());
            commentInfo.setPersonalCode("system");
            commentInfoService.saveComment(commentInfo);
        } catch (Exception e) {
            log.warn("保存流程结束评论失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }

        // 4. 删除运行时扩展信息
        try {
            extendProcinstService.deleteExtendProcinstByProcessInstanceId(processInstanceId);
        } catch (Exception e) {
            log.warn("删除运行时扩展信息失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }

        log.info("流程实例结束处理完成: processInstanceId={}", processInstanceId);
    }

    /**
     * 流程结束后更新业务记录状态
     * 通过 businessKey（即 businessNo）找到业务记录并更新状态
     */
    private void updateBusinessRecordStatus(String processInstanceId) {
        try {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (hpi == null) return;

            String businessKey = hpi.getBusinessKey();
            if (businessKey == null || businessKey.isEmpty()) return;

            // 判断审批结果：正常结束=COMPLETED，终止=REJECTED
            String status = "COMPLETED";
            if (hpi.getDeleteReason() != null && hpi.getDeleteReason().contains("reject")) {
                status = "REJECTED";
            }

            businessFeignClient.updateStatusByBusinessNo(businessKey, status);
            log.info("业务记录状态已更新: businessNo={}, status={}", businessKey, status);
        } catch (Exception e) {
            log.warn("更新业务记录状态失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }
    }

    /**
     * 通知流程发起人：流程已审批完成
     */
    private void notifyProcessInitiator(String processInstanceId) {
        try {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (hpi == null) return;

            String startUserId = hpi.getStartUserId();
            if (startUserId == null || startUserId.isEmpty()) return;

            Map<String, Object> request = new HashMap<>();
            request.put("templateCode", "HCP_WF_Complete");
            request.put("receiverId", Long.parseLong(startUserId));
            request.put("receiverName", startUserId);
            Map<String, String> variables = new HashMap<>();
            variables.put("wf_pd_name", hpi.getProcessDefinitionName() != null ? hpi.getProcessDefinitionName() : "流程");
            variables.put("businessTitle", hpi.getBusinessKey() != null ? hpi.getBusinessKey() : "");
            variables.put("wf_createrName", startUserId);
            variables.put("wf_createrTime", hpi.getStartTime() != null ? hpi.getStartTime().toString() : "");
            request.put("variables", variables);
            request.put("businessType", "WORKFLOW");
            systemFeignClient.sendMessageByTemplate(request);
            log.debug("已发送流程完成通知: processInstanceId={}, startUser={}", processInstanceId, startUserId);
        } catch (Exception e) {
            log.warn("发送流程完成通知失败: processInstanceId={}, error={}", processInstanceId, e.getMessage());
        }
    }
}
