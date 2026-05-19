package com.gmp.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义扩展实体 - 在Flowable标准流程定义基础上扩展业务属性
 * 映射表: wf_process_definition_ext
 *
 * 设计说明：
 * Flowable本身有ACT_RE_PROCDEF表存储流程定义，此表作为业务扩展表
 * 通过processDefinitionId与Flowable的流程定义关联
 * 存储中文名称、分类、绑定表单Key等业务属性
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_definition_ext")
public class WfProcessDefinition extends CommonEntity {

    /** Flowable流程定义ID（关联ACT_RE_PROCDEF.ID_） */
    private String processDefinitionId;

    /** 流程定义Key（BPMN XML中的process id） */
    private String processKey;

    /** 流程名称（中文显示） */
    private String processName;

    /** 流程分类：COMM/DMS/QMS/TMS/LIMS/MES/FMS/ERMS */
    private String category;

    /** 流程状态: DRAFT=草稿, PUBLISHED=已发布, SUSPENDED=已挂起 */
    private String status;

    /** 流程描述 */
    private String description;

    /** 绑定的表单定义Key（启动流程时自动加载此表单） */
    private String formKey;

    /** 启动表单Key（流程发起时填写的表单，可与审批表单不同） */
    private String startFormKey;

    /** 流程图标/缩略图URL */
    private String diagramUrl;

    /** 流程过期时间表达式（如 PT72H=72小时后过期） */
    private String expireExpression;

    /** 优先级（数值越大优先级越高） */
    private Integer priority;

    /** 是否允许发起人撤回 */
    private Boolean allowWithdraw;

    /** 是否允许发起人催办 */
    private Boolean allowUrge;

    /** 是否支持移动端审批 */
    private Boolean mobileEnabled;
}
