package com.gmp.workflow.resolver;

import com.gmp.framework.expression.ExpressionEngine;
import com.gmp.framework.workflow.DynamicAssigneeResolver.Strategy;
import com.gmp.workflow.feign.SystemFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态审批人解析器 — 运行时解析流程节点的审批人
 *
 * 策略：
 * 1. FIXED          — 固定人员，从流程变量 assignee 读取
 * 2. BY_ROLE        — 按角色编码查询用户列表
 * 3. BY_DEPT_LEADER — 发起人部门负责人
 * 4. BY_EXPRESSION  — SpEL表达式从表单数据提取
 * 5. BY_PARENT_DEPT — 上级部门负责人
 * 6. BY_REPORT_LINE — 部门负责人（汇报线）
 * 7. BY_MATRIX      — 矩阵组织（预留）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowAssigneeResolver {

    private final ExpressionEngine expressionEngine;
    private final SystemFeignClient systemFeignClient;

    public List<String> resolve(Strategy strategy, String expression, String roleCode,
                                Long initiatorId, Long initiatorDeptId,
                                Map<String, Object> processVariables) {
        log.info("解析审批人: strategy={}, initiator={}, dept={}", strategy, initiatorId, initiatorDeptId);

        return switch (strategy) {
            case FIXED -> resolveFixed(processVariables);
            case BY_ROLE -> resolveByRole(roleCode);
            case BY_DEPT_LEADER -> resolveByDeptLeader(initiatorDeptId);
            case BY_EXPRESSION -> resolveByExpression(expression, processVariables);
            case BY_PARENT_DEPT -> resolveByParentDept(initiatorDeptId);
            case BY_REPORT_LINE -> resolveByReportLine(initiatorId);
            case BY_MATRIX -> resolveByMatrix(initiatorId, initiatorDeptId);
        };
    }

    private List<String> resolveFixed(Map<String, Object> vars) {
        Object assignee = vars.get("assignee");
        if (assignee instanceof String s && !s.isBlank()) return List.of(s);
        if (assignee instanceof List<?> l) return l.stream().map(Object::toString).toList();
        return Collections.emptyList();
    }

    private List<String> resolveByRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return Collections.emptyList();
        try {
            var result = systemFeignClient.getUsersByRole(roleCode);
            if (result == null || result.getData() == null) return Collections.emptyList();
            return result.getData().stream()
                    .map(m -> String.valueOf(m.get("id")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("按角色查询用户失败: roleCode={}", roleCode, e);
            return Collections.emptyList();
        }
    }

    private List<String> resolveByDeptLeader(Long deptId) {
        if (deptId == null) return Collections.emptyList();
        try {
            var result = systemFeignClient.getDeptLeader(deptId);
            if (result == null || result.getData() == null) return Collections.emptyList();
            Object leaderId = result.getData().get("leaderId");
            return leaderId != null ? List.of(String.valueOf(leaderId)) : Collections.emptyList();
        } catch (Exception e) {
            log.error("查询部门负责人失败: deptId={}", deptId, e);
            return Collections.emptyList();
        }
    }

    private List<String> resolveByExpression(String expression, Map<String, Object> vars) {
        if (expression == null || expression.isBlank()) return Collections.emptyList();
        try {
            Object result = expressionEngine.evaluate(expression, vars);
            if (result instanceof String s && !s.isBlank()) return List.of(s);
            if (result instanceof List<?> l) return l.stream().map(Object::toString).toList();
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("表达式计算审批人失败: expr={}", expression, e);
            return Collections.emptyList();
        }
    }

    private List<String> resolveByParentDept(Long deptId) {
        if (deptId == null) return Collections.emptyList();
        try {
            var result = systemFeignClient.getParentDeptLeader(deptId);
            if (result == null || result.getData() == null) return Collections.emptyList();
            Object leaderId = result.getData().get("leaderId");
            return leaderId != null ? List.of(String.valueOf(leaderId)) : Collections.emptyList();
        } catch (Exception e) {
            log.error("查询上级部门负责人失败: deptId={}", deptId, e);
            return Collections.emptyList();
        }
    }

    private List<String> resolveByReportLine(Long userId) {
        if (userId == null) return Collections.emptyList();
        try {
            var result = systemFeignClient.getUserDeptLeader(userId);
            if (result == null || result.getData() == null) return Collections.emptyList();
            Object leaderId = result.getData().get("leaderId");
            return leaderId != null ? List.of(String.valueOf(leaderId)) : Collections.emptyList();
        } catch (Exception e) {
            log.error("查询汇报线失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    private List<String> resolveByMatrix(Long userId, Long deptId) {
        log.debug("矩阵式审批人解析暂未实现: userId={}, deptId={}", userId, deptId);
        return Collections.emptyList();
    }
}
