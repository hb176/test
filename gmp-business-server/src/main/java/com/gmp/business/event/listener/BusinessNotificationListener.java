package com.gmp.business.event.listener;

import com.gmp.business.entity.BusinessRecord;
import com.gmp.business.event.BusinessEventListener;
import com.gmp.common.constant.BusinessEventType;
import com.gmp.framework.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 业务通知监听器 — 关键事件发生时发送通知
 *
 * 后续接入邮件/站内信/企业微信后在此实现
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class BusinessNotificationListener implements BusinessEventListener {

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).BUSINESS_RECORD_CREATED")
    public void onBusinessCreated(EventPublisher.DomainEvent event) {
        BusinessRecord record = (BusinessRecord) event.getSource();
        log.debug("[NOTIFY] 业务创建通知（待接入消息通道）: no={}, initiator={}",
                record.getBusinessNo(), record.getInitiatorName());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).TASK_ASSIGNED")
    public void onTaskAssigned(EventPublisher.DomainEvent event) {
        log.debug("[NOTIFY] 任务分配通知（待接入消息通道）: {}", event.getPayload());
    }

    @EventListener(condition = "#event.eventType == T(com.gmp.common.constant.BusinessEventType).SLA_WARNING")
    public void onSlaWarning(EventPublisher.DomainEvent event) {
        log.warn("[NOTIFY] SLA预警（待接入消息通道）: {}", event.getPayload());
    }
}
