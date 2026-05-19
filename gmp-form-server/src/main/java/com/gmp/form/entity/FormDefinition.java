package com.gmp.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单定义实体 - 对标ct1.custom_form_info
 * 映射表: form_definition
 *
 * 设计说明（对照ct1）：
 * - EDIT_CONTENT: 编辑态表单Schema（填写/修改时的字段配置）
 * - VIEW_CONTENT: 查看态表单Schema（只读展示时的字段配置）
 * - ADD_CONTENT: 新增态表单Schema（首次创建的字段配置）
 * - JS_CONTENT: 前端JS脚本（字段联动、校验逻辑等）
 *
 * 表单范围类型（FORM_RANGE_TYPE）：
 * - RANGE_PROCESS: 流程表单（绑定到流程节点）
 * - RANGE_FUNCTION: 功能表单（独立使用，不绑定流程）
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_definition")
public class FormDefinition extends CommonEntity {

    /** 表单名称 */
    private String name;

    /** 表单编码（全局唯一，如 DEVIATION_FORM） */
    private String code;

    /** 系统模块（QMS/DMS/TMS/LIMS/MES/FMS/ERMS） */
    private String sysModule;

    /** 所属子模块 */
    private String module;

    /** 子模块名称 */
    private String moduleName;

    /** 描述 */
    private String des;

    /** 分类排序码 */
    private String sortCode;

    /** 状态（NORM=正常, DISABLED=禁用） */
    private String status;

    /** 锁定人（正在编辑该表单的用户ID） */
    private String lockUser;

    /** 表单范围类型（RANGE_PROCESS=流程表单, RANGE_FUNCTION=功能表单） */
    private String formRangeType;

    /** 表单名称是否显示（YES/NO） */
    private String formNameShow;

    /** 编辑态表单Schema（JSON） */
    private String editContent;

    /** 查看态表单Schema（JSON） */
    private String viewContent;

    /** 新增态表单Schema（JSON） */
    private String addContent;

    /** 前端JS脚本（字段联动、校验逻辑等） */
    private String jsContent;

    /** 功能表单记录ID生成规则（SpEL表达式） */
    private String funFormRecorderIdRule;

    /** 功能表单审批人配置（JSON） */
    private String funFormAuditor;

    /** 绑定的流程定义Key */
    private String workflowKey;

    /** 是否为系统内置表单 */
    private Boolean isSystem;
}
