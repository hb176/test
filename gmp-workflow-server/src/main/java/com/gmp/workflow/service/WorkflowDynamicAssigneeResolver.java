package com.gmp.workflow.service;

import com.gmp.framework.expression.ExpressionEngine;
import com.gmp.workflow.feign.SystemFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流动态审批人解析器 - 运行时动态确定流程节点的审批人
 *
 * 分配策略：
 * 1. FIXED（固定人员）：审批人由流程定义时指定
 * 2. BY_ROLE（按角色）：根据角色编码查找拥有该角色的用户
 * 3. BY_DEPT_LEADER（部门负责人）：发起人的部门负责人
 * 4. BY_EXPRESSION（表达式）：通过SpEL表达式动态计算审批人
 * 5. BY_PARENT_DEPT（上级部门）：发起人部门的上级部门负责人
 * 6. BY_REPORT_LINE（汇报线）：发起人的直接上级
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowDynamicAssigneeResolver {

    private final ExpressionEngine expressionEngine;
    private final WorkflowAssigneeService workflowAssigneeService;

    /**
     * 分配策略枚举
     */
    public enum Strategy {
        FIXED,              // 固定人员/角色
        BY_ROLE,            // 按角色
        BY_DEPT_LEADER,     // 部门负责人
        BY_EXPRESSION,      // 表达式计算
        BY_PARENT_DEPT,     // 上级部门负责人
        BY_REPORT_LINE,     // 汇报线（直接上级）
        BY_MATRIX           // 矩阵式
    }

    /**
     * 解析审批人列表
     *
     * @param strategy         分配策略
     * @param expression       表达式（策略为BY_EXPRESSION时使用）
     * @param roleCode         角色编码（策略为BY_ROLE时使用）
     * @param initiatorId      发起人ID
     * @param initiatorDeptId  发起人部门ID
     * @param processVariables 流程变量（含表单数据）
     * @return 审批人ID列表
     */
    public List<String> resolveAssignees(Strategy strategy, String expression, String roleCode,
                                          String initiatorId, Long initiatorDeptId,
                                          Map<String, Object> processVariables) {
        log.info("解析审批人 - 策略: {}, 发起人: {}, 部门: {}", strategy, initiatorId, initiatorDeptId);

        return switch (strategy) {
            case FIXED -> {
                // 固定人员：从流程变量或配置中直接获取
                Object assignee = processVariables.get("assignee");
                yield assignee != null ? List.of(assignee.toString()) : Collections.emptyList();
            }
            case BY_ROLE -> {
                // 按角色：查询拥有指定角色的用户
                if (roleCode != null && !roleCode.isBlank()) {
                    yield workflowAssigneeService.getUsersByRole(roleCode);
                }
                yield Collections.emptyList();
            }
            case BY_DEPT_LEADER -> {
                // 部门负责人：查询发起人部门的负责人
                if (initiatorDeptId != null) {
                    yield workflowAssigneeService.getDeptLeader(initiatorDeptId);
                }
                yield Collections.emptyList();
            }
            case BY_EXPRESSION -> {
                // 表达式计算：从表单数据或流程变量中提取审批人
                if (expression != null && !expression.isBlank()) {
                    Object result = expressionEngine.evaluate(expression, processVariables);
                    if (result instanceof String str) {
                        yield List.of(str);
                    } else if (result instanceof List<?> list) {
                        yield list.stream().map(Object::toString).toList();
                    }
                }
                yield Collections.emptyList();
            }
            case BY_PARENT_DEPT -> {
                // 上级部门负责人：查询发起人上级部门的负责人
                if (initiatorDeptId != null) {
                    yield workflowAssigneeService.getParentDeptLeader(initiatorDeptId);
                }
                yield Collections.emptyList();
            }
            case BY_REPORT_LINE -> {
                // 汇报线：查询发起人的直接上级
                if (initiatorId != null) {
                    yield workflowAssigneeService.getReportLineLeader(Long.parseLong(initiatorId));
                }
                yield Collections.emptyList();
            }
            case BY_MATRIX -> {
                // 矩阵式：暂未实现
                log.debug("矩阵式分配暂未实现");
                yield Collections.emptyList();
            }
        };
    }
}
