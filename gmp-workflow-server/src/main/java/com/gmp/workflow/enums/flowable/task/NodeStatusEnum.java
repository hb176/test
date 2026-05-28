package com.gmp.workflow.enums.flowable.task;

import lombok.Getter;

/**
 * 流程节点执行状态枚举
 */
@Getter
public enum NodeStatusEnum {
    PENDING("未开始"),
    PROCESSING("进行中"),
    FINISH("已完成");

    private final String description;

    NodeStatusEnum(String description) {
        this.description = description;
    }
}
