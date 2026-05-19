package com.gmp.business.dto;

import com.gmp.business.entity.BusinessRecord;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务记录响应 VO — 不直接暴露实体
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
public class BusinessRecordVO implements Serializable {

    private Long id;
    private String businessType;
    private String businessNo;
    private String title;
    private String businessStatus;
    private String formKey;
    private Long formDataId;
    private String processInstanceId;
    private String urgency;
    private String initiatorName;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private String productName;
    private String batchNo;

    /** 从实体构建 VO */
    public static BusinessRecordVO fromEntity(BusinessRecord entity) {
        BusinessRecordVO vo = new BusinessRecordVO();
        vo.setId(entity.getId());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessNo(entity.getBusinessNo());
        vo.setTitle(entity.getTitle());
        vo.setBusinessStatus(entity.getBusinessStatus());
        vo.setFormKey(entity.getFormKey());
        vo.setFormDataId(entity.getFormDataId());
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setUrgency(entity.getUrgency());
        vo.setInitiatorName(entity.getInitiatorName());
        vo.setInitiatedAt(entity.getInitiatedAt());
        vo.setCompletedAt(entity.getCompletedAt());
        vo.setProductName(entity.getProductName());
        vo.setBatchNo(entity.getBatchNo());
        return vo;
    }
}
