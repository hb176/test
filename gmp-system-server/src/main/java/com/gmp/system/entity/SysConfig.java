package com.gmp.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gmp.framework.base.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体 - 键值对形式的系统参数
 * 映射表: sys_config
 *
 * @author hb176
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends CommonEntity {

    /** 配置键，全局唯一，如 "system.site.name" */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置分类: system/security/storage/notification */
    private String category;

    /** 配置说明 */
    private String description;

    /** 是否系统内置（内置配置不可删除） */
    private Boolean isSystem;
}
