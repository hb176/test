package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.tools.common.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程实例扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tbl_flow_extend_procinst")
public class ExtendProcinst extends BaseModel {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String processInstanceId;
    private String processDefinitionId;
    private String businessKey;
    private String modelKey;
    private String processName;
    private String processStatus;
    private String currentUserCode;
    private String tenantId;
    private String userInfo;
    private String formData;
}
