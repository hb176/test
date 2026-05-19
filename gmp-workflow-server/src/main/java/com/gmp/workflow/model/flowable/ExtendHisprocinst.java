package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.tools.common.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 历史流程实例扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tbl_flow_extend_hisprocinst")
public class ExtendHisprocinst extends BaseModel {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String processInstanceId;
    private String processDefinitionId;
    private String modelKey;
    private String businessKey;
    private String processStatus;
    private String processName;
    private String currentUserCode;
    private String tenantId;
    private String userInfo;
    private String formData;

    public ExtendHisprocinst() {}
    public ExtendHisprocinst(String processInstanceId, String processStatus) {
        this.processInstanceId = processInstanceId;
        this.processStatus = processStatus;
    }
}
