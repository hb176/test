package com.gmp.workflow.enums.flowable.runtime;

import java.io.Serializable;

/** 流程状态 */
public enum ProcessStatusEnum implements Serializable {
    CG("草稿"), SPZ("审批中"), BH("驳回"), CH("撤回"), JQ("加签"),
    ZC("暂存"), ZB("转办"), BJ("办结"), ZZ("终止");

    private String msg;
    private String type;

    ProcessStatusEnum(String msg) { this.msg = msg; }

    public static String getEnumMsgByType(String type) {
        for (ProcessStatusEnum e : ProcessStatusEnum.values()) {
            if (e.toString().equals(type)) return e.msg;
        }
        return "";
    }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
