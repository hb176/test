package com.gmp.common.constant;

import lombok.Getter;

/**
 * 业务类型枚举 — 统一管理所有业务域的编码、展示名和编号前缀
 *
 * 后续新增业务类型只需在此枚举中添加一项，无需修改任何路由逻辑
 *
 * @author hb176
 * @since 1.0.0
 */
@Getter
public enum BusinessType {

    // ==================== QMS 质量过程管理 ====================
    QMS_DEVIATION("QMS_DEVIATION", "偏差管理", "DEV"),
    QMS_CAPA("QMS_CAPA", "CAPA管理", "CAPA"),
    QMS_CHANGE("QMS_CHANGE", "变更控制", "CC"),
    QMS_SUPPLIER("QMS_SUPPLIER", "供应商管理", "SUP"),
    QMS_AUDIT("QMS_AUDIT", "审计管理", "AUD"),
    QMS_COMPLAINT("QMS_COMPLAINT", "投诉处理", "COM"),
    QMS_RISK("QMS_RISK", "风险管理", "RSK"),

    // ==================== DMS 文件管理系统 ====================
    DMS_DOCUMENT("DMS_DOCUMENT", "文件管理", "DOC"),

    // ==================== TMS 培训管理系统 ====================
    TMS_COURSE("TMS_COURSE", "培训课程", "CRS"),
    TMS_PLAN("TMS_PLAN", "培训计划", "PLN"),
    TMS_RECORD("TMS_RECORD", "培训记录", "REC"),
    TMS_CERTIFICATION("TMS_CERTIFICATION", "资质管理", "CRT"),

    // ==================== QRS 质量回顾系统 ====================
    QRS_APQR("QRS_APQR", "产品质量回顾", "APQR"),
    QRS_STABILITY("QRS_STABILITY", "稳定性数据", "STB"),
    QRS_TREND("QRS_TREND", "趋势分析", "TRD");

    /** 数据库存储编码，对应 BusinessRecord.businessType */
    private final String code;
    /** 中文展示名 */
    private final String displayName;
    /** 业务编号前缀，如 DEV-2026-001 */
    private final String noPrefix;

    BusinessType(String code, String displayName, String noPrefix) {
        this.code = code;
        this.displayName = displayName;
        this.noPrefix = noPrefix;
    }

    /** 根据数据库编码查找枚举 */
    public static BusinessType fromCode(String code) {
        for (BusinessType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的业务类型编码: " + code);
    }
}
