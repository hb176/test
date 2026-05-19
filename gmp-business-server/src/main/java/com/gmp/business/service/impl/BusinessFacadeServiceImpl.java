package com.gmp.business.service.impl;

import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.AbstractBusinessService;
import com.gmp.business.service.BusinessRecordService;
import com.gmp.business.service.handler.BusinessTypeHandlerRegistry;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.framework.event.EventPublisher;
import com.gmp.framework.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务门面服务 — 控制器的统一调用入口
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Service
public class BusinessFacadeServiceImpl extends AbstractBusinessService {

    private final WorkflowService workflowService;

    public BusinessFacadeServiceImpl(BusinessTypeHandlerRegistry handlerRegistry,
                                     BusinessRecordService businessRecordService,
                                     EventPublisher eventPublisher,
                                     WorkflowService workflowService) {
        super(handlerRegistry, businessRecordService, eventPublisher);
        this.workflowService = workflowService;
    }

    @Override
    protected Long saveFormData(CreateBusinessRequest request, BusinessRecord record) {
        log.debug("表单数据占位: formKey={}, businessNo={}", request.getFormKey(), record.getBusinessNo());
        return null;
    }

    @Override
    protected String startWorkflow(BusinessRecord record, CreateBusinessRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("businessNo", record.getBusinessNo());
        variables.put("businessType", record.getBusinessType());
        variables.put("title", record.getTitle());
        variables.put("formKey", record.getFormKey());
        variables.put("initiatorId", record.getInitiatorId());
        variables.put("initiatorName", record.getInitiatorName());
        variables.put("initiatorDeptId", record.getInitiatorDeptId());
        variables.put("urgency", record.getUrgency());

        String processDefinitionKey = record.getBusinessType();

        try {
            ProcessInstance pi = workflowService.startProcess(
                    processDefinitionKey,
                    record.getBusinessNo(),
                    variables,
                    record.getInitiatorName());
            log.info("工作流已启动: businessNo={}, processInstanceId={}, definitionKey={}",
                    record.getBusinessNo(), pi.getId(), processDefinitionKey);
            return pi.getId();
        } catch (BusinessException e) {
            // 流程定义尚未部署 — 允许业务记录先保存，流程后续补启动
            log.warn("流程启动暂缓（流程定义未部署）: key={}, businessNo={}, error={}",
                    processDefinitionKey, record.getBusinessNo(), e.getMessage());
            return null;
        }
    }
}
