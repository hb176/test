package com.gmp.workflow.vo.flowable.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class StartProcessInstanceVo implements Serializable {
    @Schema(description = "流程定义key", required = true)
    private String processDefinitionKey;
    @Schema(description = "业务表单唯一标识", required = true)
    private String businessKey;
    @Schema(description = "启动流程变量")
    private Map<String, Object> variables;
    @Schema(description = "申请人工号")
    private String currentUserCode;
    @Schema(description = "系统标识", required = true)
    private String appSn;
    @Schema(description = "表单显示名称", required = true)
    private String formName;
    @Schema(description = "流程提交人工号")
    private String creator;
    @Schema(description = "老的流程实例id")
    private String oldProcessInstanceId;
    @Schema(description = "是否走底表数据")
    private boolean flowLevelFlag = true;
    @Schema(description = "要走流程部门ID")
    private String deptId;
    @Schema(description = "表单数据")
    private String formData;
}
