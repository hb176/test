package com.gmp.workflow.listener;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.model.flowable.ExtendProcinst;
import com.gmp.workflow.service.flowable.IExtendProcinstService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

/**
 * 流程实例启动全局监听器
 * <p>
 * 当子流程启动时，自动为其创建扩展信息（tbl_flow_extend_procinst），
 * 并将主流程的 businessKey 和名称同步到子流程。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalProcistStartListener extends AbstractFlowableEngineEventListener {

    private final RuntimeService runtimeService;
    private final IExtendProcinstService extendProcinstService;

    /**
     * 流程实例启动事件处理
     * <p>
     * 当检测到子流程启动时（父流程有 processInstanceId 但无 businessKey），
     * 自动创建扩展信息并从主流程同步 businessKey 和名称。
     * </p>
     *
     * @param event 流程启动事件
     */
    @Override
    protected void processStarted(FlowableProcessStartedEvent event) {
        FlowableEntityEventImpl flowableEntityEvent = (FlowableEntityEventImpl) event;
        ExecutionEntityImpl processInstance = (ExecutionEntityImpl) flowableEntityEvent.getEntity();
        ExecutionEntityImpl parent = processInstance.getParent();

        // 判断是否为子流程：父流程有 processInstanceId 但没有 businessKey
        if (StringUtils.isNotBlank(parent.getProcessInstanceId())
                && StringUtils.isBlank(parent.getBusinessKey())) {
            ProcessInstance superProcessInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstance.getRootProcessInstanceId()).singleResult();

            // 检查是否已存在扩展信息
            ExtendProcinst existExtend = extendProcinstService
                    .findExtendProcinstByProcessInstanceId(processInstance.getProcessInstanceId());
            if (existExtend == null) {
                // 创建子流程的扩展信息
                ExtendProcinst extendProcinst = new ExtendProcinst();
                extendProcinst.setProcessInstanceId(processInstance.getProcessInstanceId());
                extendProcinst.setModelKey(processInstance.getProcessDefinitionKey());
                extendProcinst.setProcessDefinitionId(processInstance.getProcessDefinitionId());
                extendProcinst.setId(IdWorker.get32UUID());
                extendProcinst.setCurrentUserCode(parent.getStartUserId());
                extendProcinst.setProcessStatus(ProcessStatusEnum.SPZ.toString());
                extendProcinstService.saveExtendProcinstAndHis(extendProcinst);

                // 将主流程的 businessKey 和名称同步到子流程
                if (superProcessInstance != null) {
                    runtimeService.updateBusinessKey(processInstance.getProcessInstanceId(),
                            superProcessInstance.getBusinessKey());
                    runtimeService.setProcessInstanceName(processInstance.getProcessInstanceId(),
                            superProcessInstance.getName());
                }
                log.info("子流程扩展信息已创建: processInstanceId={}, parentProcessId={}",
                        processInstance.getProcessInstanceId(), parent.getProcessInstanceId());
            }
        }
    }
}
