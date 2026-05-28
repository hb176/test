package com.gmp.workflow.enums.flowable.model;

/**
 * 流程模型发布状态枚举
 * <p>
 * 定义模型从草稿到发布、停用的生命周期状态。
 * 用于校验模型操作的前置状态条件。
 * </p>
 *
 * <ul>
 *   <li>CG - 草稿，模型初始状态</li>
 *   <li>DFB - 待发布，模型已配置但未发布</li>
 *   <li>YFB - 已发布，模型已部署到引擎</li>
 *   <li>TY - 停用，模型已下线</li>
 * </ul>
 */
public enum ModelFormStatusEnum {

    /** 草稿 */
    CG(1, "草稿"),
    /** 待发布 */
    DFB(2, "待发布"),
    /** 已发布 */
    YFB(3, "已发布"),
    /** 停用 */
    TY(4, "停用");

    private final Integer status;
    private final String msg;

    ModelFormStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 根据状态码获取状态名称
     *
     * @param status 状态码
     * @return 状态名称，未找到返回 null
     */
    public static String getName(Integer status) {
        if (status == null) return null;
        for (ModelFormStatusEnum item : values()) {
            if (status.equals(item.status)) return item.msg;
        }
        return null;
    }

    /**
     * 根据状态码获取枚举实例
     *
     * @param status 状态码
     * @return 枚举实例，未找到返回 null
     */
    public static ModelFormStatusEnum getEnum(Integer status) {
        if (status == null) return null;
        for (ModelFormStatusEnum item : values()) {
            if (status.equals(item.status)) return item;
        }
        return null;
    }

    /**
     * 校验模型状态和扩展状态是否允许发布
     * <p>
     * 两者都必须为"待发布"或"已发布"状态才允许执行发布操作。
     * </p>
     *
     * @param modelStatus   模型状态码
     * @param extendStatus  扩展信息状态码
     * @return true 表示状态允许发布
     */
    public static boolean checkActive(Integer modelStatus, Integer extendStatus) {
        if (modelStatus == null || extendStatus == null) return false;
        boolean modelOk = modelStatus.equals(DFB.status) || modelStatus.equals(YFB.status);
        boolean extendOk = extendStatus.equals(DFB.status) || extendStatus.equals(YFB.status);
        return modelOk && extendOk;
    }

    /**
     * 获取两个状态中的最低状态（用于汇总展示）
     *
     * @param modelStatus  模型状态码
     * @param extendStatus 扩展信息状态码
     * @return 最低状态的枚举实例
     */
    public static ModelFormStatusEnum getMinStatus(Integer modelStatus, Integer extendStatus) {
        int m = modelStatus == null ? 1 : modelStatus;
        int e = extendStatus == null ? 1 : extendStatus;
        return getEnum(Math.min(m, e));
    }
}
