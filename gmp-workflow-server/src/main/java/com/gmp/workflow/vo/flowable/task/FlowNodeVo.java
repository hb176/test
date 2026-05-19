package com.gmp.workflow.vo.flowable.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlowNodeVo implements Serializable {
    @Schema(description = "节点id")
    private String nodeId;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "执行人的工号")
    private String userCode;
    @Schema(description = "执行人姓名")
    private String userName;
    @Schema(description = "任务节点结束时间")
    private Date endTime;

    public FlowNodeVo() {}
    public FlowNodeVo(String nodeId, String nodeName, String userCode, Date endTime) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.userCode = userCode;
        this.endTime = endTime;
    }
}
