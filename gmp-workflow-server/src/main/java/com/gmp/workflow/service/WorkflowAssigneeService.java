package com.gmp.workflow.service;

import com.gmp.workflow.feign.SystemFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程审批人服务 - 根据策略查询审批人
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAssigneeService {

    private final SystemFeignClient systemFeignClient;

    /**
     * 根据角色编码查询用户列表
     *
     * @param roleCode 角色编码
     * @return 用户ID列表
     */
    public List<String> getUsersByRole(String roleCode) {
        try {
            var result = systemFeignClient.getUsersByRole(roleCode);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData().stream()
                        .map(user -> String.valueOf(user.get("id")))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("查询角色用户失败: roleCode={}", roleCode, e);
        }
        return Collections.emptyList();
    }

    /**
     * 查询部门负责人
     *
     * @param deptId 部门ID
     * @return 负责人ID列表
     */
    public List<String> getDeptLeader(Long deptId) {
        try {
            var result = systemFeignClient.getDeptLeader(deptId);
            if (result.isSuccess() && result.getData() != null) {
                Map<String, Object> leader = result.getData();
                Object leaderId = leader.get("leaderId");
                if (leaderId != null) {
                    return List.of(String.valueOf(leaderId));
                }
            }
        } catch (Exception e) {
            log.error("查询部门负责人失败: deptId={}", deptId, e);
        }
        return Collections.emptyList();
    }

    /**
     * 查询上级部门负责人
     *
     * @param deptId 当前部门ID
     * @return 上级部门负责人ID列表
     */
    public List<String> getParentDeptLeader(Long deptId) {
        try {
            var deptResult = systemFeignClient.getDeptById(deptId);
            if (deptResult.isSuccess() && deptResult.getData() != null) {
                Long parentId = Long.valueOf(String.valueOf(deptResult.getData().get("parentId")));
                if (parentId != null && parentId > 0) {
                    return getDeptLeader(parentId);
                }
            }
        } catch (Exception e) {
            log.error("查询上级部门负责人失败: deptId={}", deptId, e);
        }
        return Collections.emptyList();
    }

    /**
     * 查询用户的直接上级（汇报线）
     * 基于部门负责人的逻辑：用户所在部门的负责人即为直接上级
     *
     * @param userId 用户ID
     * @return 直接上级ID列表
     */
    public List<String> getReportLineLeader(Long userId) {
        try {
            var userResult = systemFeignClient.getUserById(userId);
            if (userResult.isSuccess() && userResult.getData() != null) {
                Object deptIdObj = userResult.getData().get("deptId");
                if (deptIdObj != null) {
                    Long deptId = Long.valueOf(String.valueOf(deptIdObj));
                    List<String> leaders = getDeptLeader(deptId);
                    // 排除自己
                    return leaders.stream()
                            .filter(id -> !id.equals(String.valueOf(userId)))
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("查询汇报线失败: userId={}", userId, e);
        }
        return Collections.emptyList();
    }
}
