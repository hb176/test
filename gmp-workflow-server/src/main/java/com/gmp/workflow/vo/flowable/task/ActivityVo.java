package com.gmp.workflow.vo.flowable.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ActivityVo implements Serializable {
    private String id;
    private double x;
    private double y;
    private double width;
    private double height;
    private String documentation;
    private String description;
    private String name;
    private String approver;
    private String type;
    private String nodeType;
    private String status;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;
    private String duration;
    private String approverNo;
    private String proceInsId;
    private String proceDefId;
    private String taskDefKey;
}
