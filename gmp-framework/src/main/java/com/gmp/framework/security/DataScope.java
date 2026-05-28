package com.gmp.framework.security;

import java.lang.annotation.*;

/**
 * 数据权限注解 - 标注在Service/Controller方法上，自动注入数据范围过滤条件
 *
 * 使用方式:
 * 1. 在查询方法上添加 @DataScope(deptAlias = "d", userAlias = "u")
 * 2. 框架会根据当前用户角色的data_scope配置自动拼接SQL条件
 *
 * @author hb176
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** 部门表的别名（用于拼接 dept_id = ? 条件） */
    String deptAlias() default "";

    /** 用户表的别名（用于拼接 create_by = ? 条件） */
    String userAlias() default "";

    /** 部门字段名（默认 dept_id） */
    String deptField() default "dept_id";

    /** 用户字段名（默认 create_by） */
    String userField() default "create_by";
}
