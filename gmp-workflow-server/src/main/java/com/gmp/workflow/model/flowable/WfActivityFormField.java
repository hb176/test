package com.gmp.workflow.model.flowable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 流程节点表单字段权限
 */
@Data
@TableName("wf_activity_form_field")
public class WfActivityFormField {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer deleted;
    private Date createTime;
    private Long createBy;
    private Date updateTime;
    private Long updateBy;

    /** 流程Key */
    private String processKey;

    /** 节点ID */
    private String activityId;

    /** 表单字段Key */
    private String fieldKey;

    /** 字段名称 */
    private String fieldName;

    /** 是否只读 */
    private Boolean readonlyFlag;

    /** 是否隐藏 */
    private Boolean hiddenFlag;

    /** 是否必填 */
    private Boolean requiredFlag;

    /** 排序号 */
    private Integer sortOrder;
}
