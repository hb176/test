package com.gmp.framework.model2table;

import java.lang.annotation.*;

/**
 * 自动建表字段注解 - 标记实体属性对应的数据库列定义
 *
 * 在AutoTable标记的类中，使用此注解标记需要映射到数据库列的字段
 * 框架在启动时会扫描这些注解并自动生成/更新数据库表结构
 *
 * 使用示例：
 * @AutoColumn(name = "user_name", type = "VARCHAR", length = 100, nullable = false, comment = "用户名")
 * private String userName;
 *
 * @author hb176
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoColumn {

    /**
     * 数据库列名（可选，默认使用字段名下划线命名）
     * 例：userName -> user_name
     */
    String name() default "";

    /**
     * 数据库列类型
     * 例：VARCHAR, INT, BIGINT, TEXT, DATETIME, DECIMAL(10,2) 等
     */
    String type() default "VARCHAR";

    /**
     * 列长度（仅对VARCHAR等类型有效）
     */
    int length() default 255;

    /**
     * 是否为主键
     */
    boolean isKey() default false;

    /**
     * 是否自动递增
     */
    boolean isAutoIncrement() default false;

    /**
     * 是否允许NULL
     */
    boolean nullable() default true;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * 是否唯一约束
     */
    boolean unique() default false;

    /**
     * 是否无符号（仅对数值类型有效）
     */
    boolean unsigned() default false;

    /**
     * 列注释
     */
    String comment() default "";

    /**
     * 是否需要索引
     */
    boolean index() default false;
}
