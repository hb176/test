package com.gmp.framework.security;

import com.gmp.common.security.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限切面 - 拦截 @DataScope 注解的方法，注入数据范围过滤条件
 *
 * 数据权限范围:
 * - ALL: 全部数据（不添加过滤条件）
 * - DEPT: 本部门数据
 * - DEPT_AND_CHILDREN: 本部门及子部门数据
 * - SELF: 仅本人创建的数据
 * - CUSTOM: 自定义部门（需配合 sys_role_dept 表）
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {

    /** ThreadLocal 存储当前方法的数据范围SQL条件 */
    private static final ThreadLocal<String> DATA_SCOPE_CONDITION = new ThreadLocal<>();

    /**
     * 获取当前方法的数据范围SQL条件
     * 在MyBatis Mapper XML 中通过 ${@link #getDataScopeCondition()} 获取
     */
    public static String getDataScopeCondition() {
        String condition = DATA_SCOPE_CONDITION.get();
        return condition != null ? condition : "";
    }

    /**
     * 清除数据范围条件（请求结束后必须调用）
     */
    public static void clear() {
        DATA_SCOPE_CONDITION.remove();
    }

    @Before("@annotation(com.gmp.framework.security.DataScope)")
    public void doBefore(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataScope ds = method.getAnnotation(DataScope.class);
        if (ds == null) return;

        // 获取当前用户上下文
        SecurityContextHolder.UserContext ctx = SecurityContextHolder.getContext();
        if (ctx == null) {
            DATA_SCOPE_CONDITION.set("");
            return;
        }

        Long currentUserId = ctx.getUserId();
        Long currentDeptId = ctx.getDeptId();

        // 管理员拥有全部数据权限
        if (ctx.getRoles() != null && ctx.getRoles().contains("ROLE_ADMIN")) {
            DATA_SCOPE_CONDITION.set("");
            return;
        }

        // TODO: 从数据库查询当前用户角色的数据权限配置
        // 目前默认管理员全部权限，普通用户本部门权限
        // 后续可从 sys_role.data_scope 查询

        // 构建数据范围条件
        String condition = buildDataScopeCondition(ds, currentUserId, currentDeptId);
        DATA_SCOPE_CONDITION.set(condition);
        log.debug("数据权限过滤: userId={}, deptId={}, condition={}", currentUserId, currentDeptId, condition);
    }

    /**
     * 构建数据范围SQL条件
     */
    private String buildDataScopeCondition(DataScope ds, Long userId, Long deptId) {
        StringBuilder sql = new StringBuilder();

        String deptAlias = ds.deptAlias();
        String userAlias = ds.userAlias();
        String deptField = ds.deptField();
        String userField = ds.userField();

        // 默认使用本部门权限（后续从数据库读取角色配置）
        String dataScope = "DEPT";

        switch (dataScope) {
            case "ALL":
                // 全部数据，不添加条件
                break;

            case "DEPT":
                // 本部门数据
                if (deptId != null && !deptAlias.isEmpty()) {
                    sql.append(String.format(" AND %s.%s = %d", deptAlias, deptField, deptId));
                } else if (deptId != null) {
                    sql.append(String.format(" AND %s = %d", deptField, deptId));
                }
                break;

            case "DEPT_AND_CHILDREN":
                // 本部门及子部门数据
                if (deptId != null && !deptAlias.isEmpty()) {
                    // 使用 ancestors 字段匹配子部门
                    sql.append(String.format(" AND (%s.%s = %d OR %s.%s IN (SELECT id FROM sys_dept WHERE ancestors LIKE CONCAT('%%',%d,',%%')))",
                            deptAlias, deptField, deptId, deptAlias, deptField, deptId));
                } else if (deptId != null) {
                    sql.append(String.format(" AND (%s = %d OR %s IN (SELECT id FROM sys_dept WHERE ancestors LIKE CONCAT('%%',%d,',%%')))",
                            deptField, deptId, deptField, deptId));
                }
                break;

            case "SELF":
                // 仅本人创建的数据
                if (userId != null && !userAlias.isEmpty()) {
                    sql.append(String.format(" AND %s.%s = %d", userAlias, userField, userId));
                } else if (userId != null) {
                    sql.append(String.format(" AND %s = %d", userField, userId));
                }
                break;

            case "CUSTOM":
                // 自定义部门（需配合 sys_role_dept 表查询）
                // TODO: 实现自定义部门权限
                break;
        }

        return sql.toString();
    }
}
