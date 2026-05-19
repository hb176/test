package com.gmp.business.service.handler;

import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.common.constant.BusinessType;

/**
 * 业务类型处理器 — 策略接口
 *
 * 每个业务类型（QMS偏差、DMS文件等）对应一个 @Component 实现，
 * 由 BusinessTypeHandlerRegistry 自动发现和路由。
 *
 * 只包含钩子方法，流程编排由 AbstractBusinessService 模板方法负责。
 *
 * @author hb176
 * @since 1.0.0
 */
public interface BusinessTypeHandler {

    /** 本处理器支持的业务类型 */
    BusinessType getSupportedType();

    /** 业务特定校验（在表单 Schema 校验之后执行） */
    default void validate(CreateBusinessRequest request) {}

    /** 生成下一个业务编号，如 DEV-2026-042 */
    String generateBusinessNo(BusinessRecord record);

    /** 数据库保存后、流程启动前的钩子 */
    default void beforeWorkflowStart(BusinessRecord record) {}

    /** 流程启动后的钩子 */
    default void afterWorkflowStart(BusinessRecord record, String processInstanceId) {}

    /** 工作流任务完成时的钩子 */
    default void onTaskCompleted(BusinessRecord record, String taskId, String outcome) {}

    /** 整个流程完成时的钩子 */
    default void onProcessCompleted(BusinessRecord record) {}

    /** 流程被驳回时的钩子 */
    default void onProcessRejected(BusinessRecord record, String reason) {}
}
