package com.gmp.workflow.vo.flowable.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "查询模型对象返回对象")
public class ModelInfoVo implements Serializable {
    private static final long serialVersionUID = -2434943659168309903L;
    private String id;
    private String modelId;
    private String modelKey;
    private String modelName;
    private String fileName;
    private String modelXml;
    private String appSn;
    private String categoryCode;
}
