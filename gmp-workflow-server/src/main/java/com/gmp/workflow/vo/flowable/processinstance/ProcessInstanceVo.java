package com.gmp.workflow.vo.flowable.processinstance;

import com.gmp.workflow.enums.flowable.runtime.ProcessStatusEnum;
import com.gmp.workflow.vo.flowable.task.ApproverVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "查询流程实例返回对象")
public class ProcessInstanceVo implements Serializable {
    private static final long serialVersionUID = -5038515846218363889L;

    public ProcessInstanceVo() {}
    public ProcessInstanceVo(String processInstanceId, String businessKey, String formName, String startedUserId) {
        this.processInstanceId = processInstanceId;
        this.businessKey = businessKey;
        this.formName = formName;
        this.startedUserId = startedUserId;
    }

    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionName;
    private String processDefinitionKey;
    private Integer processDefinitionType;
    private Integer formType;
    private Integer processDefinitionVersion;
    private String categoryCode;
    private String deploymentId;
    private String businessKey;
    private String assignees;
    private String appId;
    private String appSn;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Boolean pState;
    private String reason;
    private String startedUserId;
    private String startedUserName;
    private List<String> startedUserIds;
    private String startedUserDept;
    private String startedUserDeptName;
    private String startedUserCom;
    private String startedUserComName;
    private Boolean finishFlag = false;
    private String processStatus;
    private String processStatusName;
    private String formName;
    private String startPersonName;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private String startTimeStr;
    private String endTimeStr;
    private String appName;
    private String currentNodeName;
    private String businessUrl;
    private String totalTime;
    private List<ApproverVo> currentAssignees;
    private Integer queryType;
    private String newVersion;
    private List<String> proInstanceIdList;
    private String tableName;
}
