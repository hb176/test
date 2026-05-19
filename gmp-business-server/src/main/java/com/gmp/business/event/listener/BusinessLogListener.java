package com.gmp.business.event.listener;

import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.event.BusinessEventListener;
import com.gmp.common.constant.BusinessEventType;
import com.gmp.framework.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 业务操作审计日志监听器 — 将业务关键操作写入日志（后续可接入 SysOperLog）
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class BusinessLogListener implements BusinessEventListener {

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).BUSINESS_RECORD_CREATED")
    public void onBusinessRecordCreated(EventPublisher.DomainEvent event) {
        BusinessRecord record = (BusinessRecord) event.getSource();
        log.info("[AUDIT] 业务记录创建: type={}, no={}, title={}, initiator={}",
                record.getBusinessType(), record.getBusinessNo(),
                record.getTitle(), record.getInitiatorName());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).PROCESS_STARTED")
    public void onProcessStarted(EventPublisher.DomainEvent event) {
        log.info("[AUDIT] 流程启动: source={}", event.getSource());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).PROCESS_COMPLETED")
    public void onProcessCompleted(EventPublisher.DomainEvent event) {
        log.info("[AUDIT] 流程完成: source={}", event.getSource());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).PROCESS_REJECTED")
    public void onProcessRejected(EventPublisher.DomainEvent event) {
        log.info("[AUDIT] 流程驳回: source={}", event.getSource());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).BUSINESS_RECORD_COMPLETED")
    public void onBusinessRecordCompleted(EventPublisher.DomainEvent event) {
        BusinessRecord record = (BusinessRecord) event.getSource();
        log.info("[AUDIT] 业务记录完成: type={}, no={}",
                record.getBusinessType(), record.getBusinessNo());
    }
}
