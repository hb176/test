package com.gmp.common.constant;

/**
 * 业务事件类型常量 — 供 EventPublisher 和 @EventListener 使用
 *
 * @author hb176
 * @since 1.0.0
 */
public final class BusinessEventType {

    private BusinessEventType() {}

    /** 业务记录创建 */
    public static final String BUSINESS_RECORD_CREATED = "BUSINESS_RECORD_CREATED";
    /** 业务记录完成 */
    public static final String BUSINESS_RECORD_COMPLETED = "BUSINESS_RECORD_COMPLETED";
    /** 业务记录驳回 */
    public static final String BUSINESS_RECORD_REJECTED = "BUSINESS_RECORD_REJECTED";

    /** 表单提交 */
    public static final String FORM_SUBMITTED = "FORM_SUBMITTED";

    /** 流程启动 */
    public static final String PROCESS_STARTED = "PROCESS_STARTED";
    /** 流程完成 */
    public static final String PROCESS_COMPLETED = "PROCESS_COMPLETED";
    /** 流程驳回 */
    public static final String PROCESS_REJECTED = "PROCESS_REJECTED";

    /** 任务分配 */
    public static final String TASK_ASSIGNED = "TASK_ASSIGNED";
    /** 任务完成 */
    public static final String TASK_COMPLETED = "TASK_COMPLETED";

    /** SLA预警 */
    public static final String SLA_WARNING = "SLA_WARNING";
    /** SLA升级 */
    public static final String SLA_ESCALATION = "SLA_ESCALATION";
}
