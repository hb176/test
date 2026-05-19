package com.gmp.workflow.vo.flowable.comment;

import com.gmp.workflow.enums.flowable.runtime.CommentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlowCommentVo implements Serializable {
    @Schema(description = "任务id")
    protected String taskId;
    @Schema(description = "添加人")
    protected String personalCode;
    @Schema(description = "用户的名称")
    protected String userName;
    @Schema(description = "用户的头像链接")
    protected String userUrl;
    @Schema(description = "流程实例id")
    protected String processInstanceId;
    @Schema(description = "意见信息")
    protected String message;
    @Schema(description = "时间")
    protected Date time;
    @Schema(description = "类型CODE")
    private String type;
    @Schema(description = "类型名称")
    private String typeName;
    @Schema(description = "任务名称")
    private String taskName;
    @Schema(description = "评论信息")
    private String fullMsg;

    public FlowCommentVo() {}
    public FlowCommentVo(String personalCode, String processInstanceId, String message, String type) {
        this.personalCode = personalCode; this.processInstanceId = processInstanceId;
        this.message = message; this.type = type;
    }
    public FlowCommentVo(String taskId, String personalCode, String processInstanceId, String message, String type) {
        this.taskId = taskId; this.personalCode = personalCode;
        this.processInstanceId = processInstanceId; this.message = message; this.type = type;
    }
}
