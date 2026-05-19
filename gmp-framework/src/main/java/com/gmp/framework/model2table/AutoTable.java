package com.gmp.framework.model2table;

import java.lang.annotation.*;

/**
 * 自动建表注解 - 标记在实体类上，标识该类需要自动映射为数据库表
 *
 * 配合AutoColumn注解使用，在应用启动时自动检查并创建/更新数据库表结构
 *
 * 使用示例：
 * @AutoTable(name = "sys_user", comment = "系统用户表")
 * public class SysUser extends CommonEntity { ... }
 *
 * @author hb176
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoTable {

    /**
     * 表名（必填）
     * 映射到数据库中的表名
     */
    String name();

    /**
     * 表注释（可选）
     * 用于生成表的COMMENT说明
     */
    String comment() default "";
}
