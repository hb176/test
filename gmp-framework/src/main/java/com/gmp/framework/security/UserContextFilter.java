package com.gmp.framework.security;

import com.gmp.common.security.SecurityContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 用户上下文过滤器 - 从请求头中提取用户信息并设置到SecurityContextHolder
 *
 * 由网关层通过请求头传递用户信息:
 * - X-User-Id: 用户ID
 * - X-User-Name: 用户名
 * - X-User-Roles: 用户角色（逗号分隔）
 * - X-User-Dept-Id: 部门ID
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String userIdStr = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-User-Name");
            String rolesStr = httpRequest.getHeader("X-User-Roles");
            String deptIdStr = httpRequest.getHeader("X-User-Dept-Id");

            if (userIdStr != null && !userIdStr.isEmpty()) {
                Long userId = Long.parseLong(userIdStr);
                Long deptId = (deptIdStr != null && !deptIdStr.isEmpty()) ? Long.parseLong(deptIdStr) : null;
                List<String> roles = (rolesStr != null && !rolesStr.isEmpty())
                        ? Arrays.asList(rolesStr.split(","))
                        : List.of();

                SecurityContextHolder.UserContext context = new SecurityContextHolder.UserContext();
                context.setUserId(userId);
                context.setUsername(username);
                context.setRoles(roles);
                context.setDeptId(deptId);

                SecurityContextHolder.setContext(context);
                log.debug("用户上下文已设置: userId={}, username={}, deptId={}, roles={}", userId, username, deptId, roles);
            }

            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
            DataScopeAspect.clear();
        }
    }
}
