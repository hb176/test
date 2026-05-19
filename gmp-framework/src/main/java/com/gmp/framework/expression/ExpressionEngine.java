package com.gmp.framework.expression;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 表达式引擎 - 支持SpEL和自定义函数，用于表单计算和流程条件判断
 *
 * 使用场景：
 * 1. 表单字段联动计算（如：总价 = 单价 * 数量）
 * 2. 表单字段条件显示/隐藏（如：选择"其他"时显示"请说明"文本框）
 * 3. 流程分支条件判断（如：金额 > 10000 时走总经理审批）
 * 4. 数据校验自定义规则（如：结束日期 > 开始日期）
 *
 * SpEL表达式示例：
 * - 算术: #price * #quantity
 * - 条件: #amount > 10000 ? 'HIGH' : 'LOW'
 * - 逻辑: #status == 'APPROVED' && #level >= 3
 * - 字符串: #name != null && #name.length() > 0
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
public class ExpressionEngine {

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 执行SpEL表达式并返回结果
     *
     * @param expression SpEL表达式字符串
     * @param variables  变量上下文（表单字段值、流程变量等）
     * @return 表达式计算结果
     */
    public Object evaluate(String expression, Map<String, Object> variables) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            return parser.parseExpression(expression).getValue(context);
        } catch (Exception e) {
            log.error("表达式执行失败: {} - 变量: {}", expression, variables, e);
            throw new RuntimeException("表达式计算错误: " + e.getMessage(), e);
        }
    }

    /**
     * 执行布尔表达式（用于流程条件判断）
     *
     * @param expression 布尔表达式
     * @param variables  变量上下文
     * @return true/false
     */
    public boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        Object result = evaluate(expression, variables);
        if (result instanceof Boolean b) {
            return b;
        }
        log.warn("表达式未返回布尔值: {} -> {}", expression, result);
        return false;
    }

    /**
     * 执行字符串表达式
     */
    public String evaluateString(String expression, Map<String, Object> variables) {
        Object result = evaluate(expression, variables);
        return result != null ? result.toString() : null;
    }

    /**
     * 从JSONObject构建变量上下文并执行表达式
     * 用于表单数据计算（如：totalAmount = price * quantity）
     */
    public Object evaluateWithJSON(String expression, JSONObject formData) {
        Map<String, Object> variables = formData.toJavaObject(Map.class);
        return evaluate(expression, variables);
    }

    /**
     * 验证表达式语法是否合法
     *
     * @param expression SpEL表达式
     * @return true=合法, false=不合法
     */
    public boolean validateExpression(String expression) {
        try {
            parser.parseExpression(expression);
            return true;
        } catch (Exception e) {
            log.warn("表达式语法不合法: {} - {}", expression, e.getMessage());
            return false;
        }
    }
}
