package com.gmp.workflow.enums.flowable.task;

import lombok.Getter;

/**
 * 流程节点类型枚举
 * <p>
 * 与 BPMN 自定义属性 nodetype 对应，标识节点的业务角色。
 * </p>
 */
@Getter
public enum NodeTypeEnum {
    APPLY("apply", "审批节点"),
    APPLYING("applying", "申请节点"),
    NOTIFY("notify", "知会节点"),
    NOAPPROVE("noapprove", "无需审批"),
    COORDINATION("coordination", "协办节点"),
    REVIEW("review", "审核节点");

    private final String type;
    private final String description;

    NodeTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }
}
