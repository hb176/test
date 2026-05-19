package com.gmp.workflow.vo.flowable.task;

import com.gmp.workflow.tools.pager.ORDERBY;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "查询任务实例的参数")
public class TaskQueryParamsVo implements Serializable {
    @Schema(description = "用户的工号", example = "00004737")
    @NotNull(message = "工号必须不为空")
    private String userCode;
    @Schema(description = "系统标识", example = "flow")
    private String appSn;
    @Schema(description = "系统标识列表", example = "flow itp")
    private List<String> appSns;
    @Schema(description = "表单名称", example = "费用报销")
    private String formName;
    @Schema(description = "开始时间", example = "2020-08-05 08:12:41")
    private String startTime;
    @Schema(description = "结束时间", example = "2020-08-05 08:12:41")
    private String endTime;
    @Schema(description = "业务主键", example = "1234656")
    private String businessKey;
    @Schema(description = "待办人", example = "00004737")
    private String assignee;
    @Schema(description = "排序字段 1 升序  0 降序", example = "0")
    private Integer orderFlag = 0;
    @Schema(description = "流程实例ID")
    private String processInstanceId;
    @Schema(description = "流程模型key", example = "modelKey")
    private String modelKey;
    @Schema(description = "流程状态 1 审批中，2结束", example = "1")
    private Integer flowStatus;
    @Schema(description = "查询关键字", example = "财务")
    private String keyword;
    @Schema(description = "节点名称", example = "部门经理")
    private String taskName;
    @Schema(description = "排序 key：字段 value 是规则")
    private Map<String, ORDERBY> orderbyMap;
}
