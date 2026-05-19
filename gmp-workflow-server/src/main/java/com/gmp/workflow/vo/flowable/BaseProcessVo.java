package com.gmp.workflow.vo.flowable;

import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public abstract class BaseProcessVo implements Serializable {
    @Schema(description = "任务id", required = true)
    private String taskId;
    @Schema(description = "节点id")
    private String activityId;
    @Schema(description = "节点名称")
    private String activityName;
    @Schema(description = "流程实例状态", required = true)
    private ProcessStatusEnum processStatusEnum;
    @Schema(description = "操作人code", required = true)
    private String userCode;
    @Schema(description = "审批意见", required = true)
    private String message;
    @Schema(description = "审批意见类型", required = true)
    private CommentTypeEnum commentTypeEnum;
    @Schema(description = "流程实例的id", required = true)
    private String processInstanceId;
}
