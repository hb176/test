package com.gmp.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 创建业务记录请求 DTO — 替换硬编码 Map<String,Object>
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
public class CreateBusinessRequest implements Serializable {

    /** 业务类型编码，对应 BusinessType.code */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /** 业务标题 */
    @NotBlank(message = "业务标题不能为空")
    private String title;

    /** 关联表单定义 Key（非表单业务可为空） */
    private String formKey;

    /** 表单填写数据（非表单业务可为空） */
    private Map<String, Object> formData;

    /** 紧急程度: NORMAL / URGENT / CRITICAL */
    private String urgency;

    /** 关联产品ID */
    private Long productId;

    /** 关联产品名称 */
    private String productName;

    /** 关联批次号 */
    private String batchNo;

    /** 标签列表 */
    private List<String> tags;

    /** 业务摘要/描述 */
    private String summary;

    /** 操作原因（作废原因/驳回原因等） */
    private String reason;
}
