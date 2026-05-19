package com.gmp.workflow.enums.flowable.task;

import java.io.Serializable;

/** 任务类型 */
public enum TaskTypeEnum implements Serializable {
    ZB("转办"), QJQ("前加签"), HJQ("后加签");

    private String type;

    TaskTypeEnum(String type) { this.type = type; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
