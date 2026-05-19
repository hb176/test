package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统字典实体 - 统一管理下拉选项和枚举值
 * 映射表: sys_dict
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends CommonEntity {

    /** 字典编码，如 GENDER, PROCESS_STATUS */
    private String dictCode;

    /** 字典名称，如 性别, 流程状态 */
    private String dictName;

    /** 字典项标签：显示用文本 */
    private String itemLabel;

    /** 字典项值：存储用值 */
    private String itemValue;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态: 1=启用, 0=禁用 */
    private Integer status;

    /** 前端CSS样式类名 */
    private String cssClass;

    /** 备注 */
    private String remark;
}
