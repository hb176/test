package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.tools.common.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 模型扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tbl_flow_model_info")
public class ModelInfo extends BaseModel implements Serializable {
    private static final long serialVersionUID = -974572277155384236L;
    public static final int CUSTOM_MODEL_TYPE = 0;
    public static final int BIZ_MODEL_TYPE = 1;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String modelId;
    private String name;
    private String modelKey;
    private Integer modelType;
    private Integer formType;
    private String appSn;
    @TableField(exist = false)
    private String appName;
    private String categoryCode;
    @TableField(exist = false)
    private String categoryName;
    private Integer status;
    @TableField(exist = false)
    private String statusName;
    private Integer extendStatus;
    @TableField(exist = false)
    private String extendStatusName;
    private String ownDeptId;
    private String ownDeptName;
    private String flowOwnerNo;
    private String flowOwnerName;
    private String processDockingNo;
    private String processDockingName;
    private String applyCompanies;
    private String showStatus;
    private Integer appliedRange;
    @TableField(exist = false)
    private String appliedRangeName;
    private String authPointList;
    private String superuser;
    private String businessUrl;
    private Integer skipSet;
    private String modelIcon;
    private Integer orderNo;
    @TableField(exist = false)
    private List<String> categoryCodes;
    @TableField(exist = false)
    private String processDefinitionId;
    @TableField(exist = false)
    private String modelXml;
    @TableField(exist = false)
    private Integer version;
    @TableField(exist = false)
    private String companyId;
}
