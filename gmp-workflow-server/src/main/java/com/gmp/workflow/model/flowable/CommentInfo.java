package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.tools.common.BaseModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审批意见信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tbl_flow_comment_info")
public class CommentInfo extends BaseModel {
    @TableId(type = IdType.ASSIGN_UUID)
    protected String id;
    protected String type;
    @TableField(exist = false)
    protected String typeName;
    protected String personalCode;
    @TableField(exist = false)
    protected String personalName;
    @TableField(exist = false)
    protected String personalHeadImg;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date time;
    protected String taskId;
    protected String activityId;
    protected String activityName;
    protected String processInstanceId;
    protected String action;
    protected String message;

    public CommentInfo(String type, String personalCode, String processInstanceId, String message) {
        this.type = type; this.personalCode = personalCode;
        this.processInstanceId = processInstanceId; this.message = message;
    }
    public CommentInfo(String type, String personalCode, String taskId, String processInstanceId, String message) {
        this.type = type; this.personalCode = personalCode;
        this.taskId = taskId; this.processInstanceId = processInstanceId; this.message = message;
    }
}
