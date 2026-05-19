package com.gmp.workflow.enums.flowable.runtime;

import java.io.Serializable;

/** 审批意见类型 */
public enum CommentTypeEnum implements Serializable {
    SP("审批"),    QS("签收"),    FQS("反签收"),  BH("驳回"),
    CH("撤回"),    ZC("暂存"),    XTZX("系统后台执行"), TJ("提交"),
    CXTJ("重新提交"), SPJS("审批结束"), LCZZ("流程终止"), WP("委派"),
    ZH("知会"),    ZY("转阅"),    YY("已阅"),    ZB("转办"),
    SQ("授权"),    SPBJQ("审批并加签"), HJQ("后加签"), QJQ("前加签"),
    CFTG("重复跳过"), XT("协同"),   PS("评审");

    private String name;

    CommentTypeEnum(String name) { this.name = name; }

    public static String getEnumMsgByType(String type) {
        for (CommentTypeEnum e : CommentTypeEnum.values()) {
            if (e.toString().equals(type)) return e.name;
        }
        return "";
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
