package com.gmp.business.service;

import com.gmp.business.dto.BusinessRecordVO;
import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.service.handler.BusinessTypeHandler;
import com.gmp.business.service.handler.BusinessTypeHandlerRegistry;
import com.gmp.common.constant.BusinessEventType;
import com.gmp.common.constant.BusinessType;
import com.gmp.common.constant.SystemConstants;
import com.gmp.common.security.SecurityContextHolder;
import com.gmp.framework.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 业务记录模板方法 — 定义业务流程骨架
 *
 * 所有业务类型共用的生命周期：
 * 1. 业务校验（表单Schema校验 + 业务特定校验）
 * 2. 构建并保存 BusinessRecord
 * 3. 保存表单数据
 * 4. 启动工作流
 * 5. 发布事件
 *
 * 步骤顺序由 final createBusinessRecord() 固定，子类通过钩子方法注入差异。
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractBusinessService {

    protected final BusinessTypeHandlerRegistry handlerRegistry;
    protected final BusinessRecordService businessRecordService;
    protected final EventPublisher eventPublisher;

    protected AbstractBusinessService(BusinessTypeHandlerRegistry handlerRegistry,
                                      BusinessRecordService businessRecordService,
                                      EventPublisher eventPublisher) {
        this.handlerRegistry = handlerRegistry;
        this.businessRecordService = businessRecordService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 模板方法 — 创建业务记录的标准流程（子类不可覆写）
     */
    @Transactional(rollbackFor = Exception.class)
    public BusinessRecordVO createBusinessRecord(CreateBusinessRequest request) {
        BusinessType businessType = BusinessType.fromCode(request.getBusinessType());
        BusinessTypeHandler handler = handlerRegistry.getHandler(businessType);

        // 1. 业务特定校验
        handler.validate(request);

        // 2. 构建实体并保存
        BusinessRecord record = buildRecord(request, businessType);
        record.setBusinessNo(handler.generateBusinessNo(record));
        businessRecordService.save(record);

        // 3. 保存表单数据（由子类实现）
        Long formDataId = saveFormData(request, record);
        record.setFormDataId(formDataId);
        businessRecordService.updateById(record);

        // 4. 启动工作流（由子类实现）
        handler.beforeWorkflowStart(record);
        String processInstanceId = startWorkflow(record, request);
        record.setProcessInstanceId(processInstanceId);
        record.setBusinessStatus(SystemConstants.PROCESS_STATUS_APPROVING);
        businessRecordService.updateById(record);
        handler.afterWorkflowStart(record, processInstanceId);

        // 5. 发布事件
        eventPublisher.publish(BusinessEventType.BUSINESS_RECORD_CREATED, record,
                Map.of("businessType", businessType.getCode(),
                        "businessNo", record.getBusinessNo()));

        log.info("业务记录已创建: type={}, no={}, id={}",
                businessType.getCode(), record.getBusinessNo(), record.getId());
        return BusinessRecordVO.fromEntity(record);
    }

    // ---- 钩子方法 ----

    /** 构建实体（含当前用户上下文） */
    protected BusinessRecord buildRecord(CreateBusinessRequest request, BusinessType type) {
        BusinessRecord record = new BusinessRecord();
        record.setBusinessType(type.getCode());
        record.setTitle(request.getTitle());
        record.setFormKey(request.getFormKey());
        record.setUrgency(request.getUrgency() != null ? request.getUrgency() : "NORMAL");
        record.setProductId(request.getProductId());
        record.setProductName(request.getProductName());
        record.setBatchNo(request.getBatchNo());
        record.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        record.setInitiatorId(SecurityContextHolder.getCurrentUserId());
        record.setInitiatorName(SecurityContextHolder.getCurrentUsername());
        record.setInitiatorDeptId(SecurityContextHolder.getCurrentDeptId());
        record.setInitiatedAt(LocalDateTime.now());
        record.setBusinessStatus(SystemConstants.PROCESS_STATUS_DRAFT);
        return record;
    }

    /** 保存表单数据 — 子类实现（调用 form-server 或本地 form_data 表） */
    protected abstract Long saveFormData(CreateBusinessRequest request, BusinessRecord record);

    /** 启动工作流 — 子类实现 */
    protected abstract String startWorkflow(BusinessRecord record, CreateBusinessRequest request);
}
