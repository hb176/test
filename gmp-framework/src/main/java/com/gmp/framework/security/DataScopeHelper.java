package com.gmp.framework.security;

/**
 * 数据权限辅助类 - 用于MyBatis XML中获取数据范围SQL条件
 *
 * 在Mapper XML中使用方式:
 * <if test="@com.gmp.framework.security.DataScopeHelper@getDataScopeCondition() != ''">
 *     ${@com.gmp.framework.security.DataScopeHelper@getDataScopeCondition()}
 * </if>
 *
 * @author hb176
 * @since 1.0.0
 */
public class DataScopeHelper {

    /**
     * 获取当前方法的数据范围SQL条件
     * 由 DataScopeAspect 在方法执行前设置
     *
     * @return SQL条件字符串（如 " AND dept_id = 1"），无条件返回空字符串
     */
    public static String getDataScopeCondition() {
        return DataScopeAspect.getDataScopeCondition();
    }
}
