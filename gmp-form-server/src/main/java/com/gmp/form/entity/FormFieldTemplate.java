package com.gmp.form.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单字段模板实体 - 预定义的字段模板，加速表单设计
 * 映射表: form_field_template
 *
 * 使用场景：创建表单时，从模板库中选择常用字段，快速完成表单设计
 * 预置模板示例：标题、编号、日期、部门、人员选择器、附件上传等
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("form_field_template")
public class FormFieldTemplate extends CommonEntity {

    /** 字段Key（模板唯一标识，如 field_title, field_date） */
    private String fieldKey;

    /** 字段名称 */
    private String fieldName;

    /** 字段类型: TEXT/TEXTAREA/NUMBER/DATE/SELECT/FILE/TABLE... */
    private String fieldType;

    /** 字段分类: BASIC=基础字段, BUSINESS=业务字段, SYSTEM=系统字段 */
    private String category;

    /** 默认字段配置（JSON格式：校验规则、默认值、提示信息等） */
    private String defaultConfig;

    /** 是否系统预置模板 */
    private Boolean isSystem;
}
