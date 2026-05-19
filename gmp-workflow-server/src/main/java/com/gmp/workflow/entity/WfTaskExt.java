package com.gmp.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务扩展实体 - 在Flowable标准任务基础上扩展业务属性
 * 映射表: wf_task_ext
 *
 * 扩展属性说明：
 * - 任务类型：主办(MAJOR)/协办(ASSIST)/会签(COUNTERSIGN)
 * - 加签信息：记录加签链
 * - 委派信息：记录委派关系
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_task_ext")
public class WfTaskExt extends CommonEntity {

    /** Flowable任务ID（关联ACT_RU_TASK.ID_） */
    private String taskId;

    /** 关联的流程实例扩展ID */
    private Long processInstanceExtId;

    /** 任务类型: MAJOR=主办, ASSIST=协办, COUNTERSIGN=会签 */
    private String taskType;

    /** 任务名称（冗余Flowable任务名称） */
    private String taskName;

    /** 审批人ID（冗余） */
    private String assignee;

    /** 审批人显示名称 */
    private String assigneeName;

    /** 审批意见 */
    private String comment;

    /** 审批结果: APPROVED=同意, REJECTED=驳回, DELEGATED=已委派, TRANSFERRED=已转办 */
    private String approveResult;

    /** 完成任务耗时（分钟） */
    private Long durationMinutes;

    /** 是否加签任务 */
    private Boolean isAddSign;

    /** 加签来源任务ID */
    private String addSignFromTaskId;

    /** 是否委派任务 */
    private Boolean isDelegated;

    /** 委派人ID（原始审批人） */
    private String delegatorId;

    /** 委派人名称 */
    private String delegatorName;

    /** 是否超时完成 */
    private Boolean isOverdue;

    /** 关联的表单定义Key（节点级别的自定义表单） */
    private String nodeFormKey;

    /** 节点级别的只读字段配置（JSON数组） */
    private String readonlyFields;

    /** 节点级别的隐藏字段配置（JSON数组） */
    private String hiddenFields;
}
