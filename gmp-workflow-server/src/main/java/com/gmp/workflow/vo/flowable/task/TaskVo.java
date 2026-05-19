package com.gmp.workflow.vo.flowable.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TaskVo implements Serializable {
    private String businessKey;
    private String name;
    private String taskId;
    private String taskDefKey;
    private String assignee;
    private String assigneeName;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String stayHour;
    private String processInstanceId;
    private String parentId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionType;
    private Integer formType;
    private String processStatus;
    private String processStatusName;
    private String taskType;
    private int status = 0;
    private String userId;
    private String userName;
    private List<String> groupIds;
    private Boolean finished = false;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishedTime;
    private String formName;
    private String startPersonCode;
    private String startPersonName;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private String appName;
    private String totalTime;
    private String businessUrl;
    private Boolean candidateFlag = false;
    private Integer type = 0;
    private String currentAssignees;
    private String currentAssigneeNos;
}
