package com.gmp.framework.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 领域事件发布器 - 微服务间异步事件通信
 *
 * 设计目的：
 * 1. 解耦服务间依赖：通过事件驱动代替直接RPC调用
 * 2. 异步处理：耗时操作（发送通知、更新索引等）不阻塞主流程
 * 3. 审计追溯：所有事件持久化，支持回溯和重放
 *
 * 事件类型（对应DTQ业务）：
 * - FORM_SUBMITTED: 表单提交（触发流程启动）
 * - PROCESS_STARTED: 流程已启动
 * - PROCESS_COMPLETED: 流程已完成（更新表单状态、发送通知）
 * - PROCESS_REJECTED: 流程已驳回（通知发起人）
 * - TASK_ASSIGNED: 任务已分配（发送待办通知）
 * - TASK_COMPLETED: 任务已完成（更新流程进度）
 * - SLA_WARNING: SLA预警（发送催办通知）
 * - SLA_ESCALATION: SLA升级（通知上级）
 * - FILE_UPLOADED: 文件已上传（病毒扫描、格式转换）
 *
 * 事件传播方式：
 * - 本地：Spring ApplicationEvent（同步/异步）
 * - 跨服务：RabbitMQ / Kafka
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布领域事件
     *
     * @param eventType 事件类型
     * @param source    事件源（通常是触发事件的实体）
     * @param payload   事件负载数据
     */
    public void publish(String eventType, Object source, Object payload) {
        DomainEvent event = new DomainEvent(source, eventType, payload);
        applicationEventPublisher.publishEvent(event);
        log.debug("事件已发布 - 类型: {}, 源: {}", eventType, source.getClass().getSimpleName());
    }

    /**
     * 发布事件（带事件数据Map）
     */
    public void publish(String eventType, Object source, java.util.Map<String, Object> payload) {
        publish(eventType, source, (Object) payload);
    }

    /**
     * 领域事件模型
     */
    public static class DomainEvent {
        /** 事件类型标识 */
        private final String eventType;
        /** 事件源对象 */
        private final Object source;
        /** 事件负载数据 */
        private final Object payload;
        /** 事件发生时间 */
        private final LocalDateTime occurredAt;

        public DomainEvent(Object source, String eventType, Object payload) {
            this.source = source;
            this.eventType = eventType;
            this.payload = payload;
            this.occurredAt = LocalDateTime.now();
        }

        public String getEventType() { return eventType; }
        public Object getSource() { return source; }
        public Object getPayload() { return payload; }
        public LocalDateTime getOccurredAt() { return occurredAt; }

        @Override
        public String toString() {
            return "DomainEvent{" +
                    "type='" + eventType + '\'' +
                    ", source=" + source.getClass().getSimpleName() +
                    ", at=" + occurredAt +
                    '}';
        }
    }
}
