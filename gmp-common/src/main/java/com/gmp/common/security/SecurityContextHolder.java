package com.gmp.common.security;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 安全上下文持有者 - 基于ThreadLocal保存当前请求的用户身份信息
 * 线程隔离，保证并发安全。每个HTTP请求由独立的线程处理，
 * 在Gateway/Filter中设置，在Service层任意位置获取
 *
 * 必须在请求结束后调用clear()清除ThreadLocal，防止内存泄漏
 *
 * @author hb176
 * @since 1.0.0
 */
public class SecurityContextHolder {

    /**
     * ThreadLocal存储用户上下文信息
     * 使用InheritableThreadLocal支持子线程继承（异步任务场景）
     */
    private static final ThreadLocal<UserContext> CONTEXT = new InheritableThreadLocal<>();

    /**
     * 设置当前请求的用户上下文
     * @param userContext 用户上下文信息
     */
    public static void setContext(UserContext userContext) {
        CONTEXT.set(userContext);
    }

    /**
     * 获取当前请求的用户上下文
     * @return 用户上下文，可能为null（未认证场景）
     */
    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     * @return 用户ID，未认证返回null
     */
    public static Long getCurrentUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户名
     * @return 用户名，未认证返回null
     */
    public static String getCurrentUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前用户角色列表
     * @return 角色列表，未认证返回null
     */
    public static List<String> getCurrentRoles() {
        UserContext context = getContext();
        return context != null ? context.getRoles() : null;
    }

    /**
     * 获取当前用户所属部门ID
     * @return 部门ID，未认证返回null
     */
    public static Long getCurrentDeptId() {
        UserContext context = getContext();
        return context != null ? context.getDeptId() : null;
    }

    /**
     * 清除ThreadLocal - 必须在请求处理完成后调用
     * 通常在Filter的finally块中调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 用户上下文信息模型
     * 存储当前请求的用户身份、权限、组织信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContext {
        /** 用户ID */
        private Long userId;
        /** 用户名 */
        private String username;
        /** 用户显示名称 */
        private String displayName;
        /** 角色编码列表 */
        private List<String> roles;
        /** 权限编码列表 */
        private List<String> permissions;
        /** 所属部门ID */
        private Long deptId;
        /** 所属部门名称 */
        private String deptName;
        /** 所属公司ID（集团场景） */
        private Long companyId;
        /** 所属公司名称 */
        private String companyName;
        /** Token签发时间戳 */
        private Long tokenIssuedAt;

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
