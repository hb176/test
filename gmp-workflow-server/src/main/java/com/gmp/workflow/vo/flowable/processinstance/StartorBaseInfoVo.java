package com.gmp.workflow.vo.flowable.processinstance;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StartorBaseInfoVo implements Serializable {
    private String processInstanceId;
    private String modelKey;
    private String modelName;
    private String businessKey;
    private String formName;
    private JSONObject starterInfo;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
