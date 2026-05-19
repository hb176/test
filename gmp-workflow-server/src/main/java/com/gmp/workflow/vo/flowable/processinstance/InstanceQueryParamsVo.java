package com.gmp.workflow.vo.flowable.processinstance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "查询流程实例的参数")
public class InstanceQueryParamsVo implements Serializable {
    @Schema(description = "用户的工号")
    private String userCode;
    @Schema(description = "应用标识")
    private String appSn;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "业务系统的id")
    private String businessKey;
    @Schema(description = "排序字段 1 升序 0 降序")
    private Integer orderFlag = 0;
    @Schema(description = "流程实例ID")
    private String processInstanceId;
    @Schema(description = "发起人工号集合")
    private String startedUserIds;
    @Schema(description = "流程定义KEY")
    private String processDefinitionKey;
    @Schema(description = "流程状态")
    private String processType;
    @Schema(description = "发起人所在部门ID")
    private String deptId;
    @Schema(description = "发起人所在公司ID")
    private String companyId;
    @Schema(description = "查询关键字")
    private String keyword;
}
