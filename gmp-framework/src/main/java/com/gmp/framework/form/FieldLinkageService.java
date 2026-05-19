package com.gmp.framework.form;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gmp.framework.expression.ExpressionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 字段联动服务 - 可配置表单的字段间联动逻辑
 *
 * 联动类型：
 * 1. VALUE_LINKAGE（值联动）：A字段值变化 → B字段值自动计算
 *    例：总金额 = 单价 * 数量
 * 2. VISIBILITY_LINKAGE（可见性联动）：A字段满足条件 → B字段显示/隐藏
 *    例：选择"其他"时，显示"请说明"字段
 * 3. VALIDATION_LINKAGE（校验联动）：A字段值变化 → B字段校验规则变化
 *    例：类型为"药品"时，批号必填
 * 4. OPTIONS_LINKAGE（选项联动）：A字段值变化 → B字段下拉选项变化
 *    例：省份变化 → 城市下拉选项联动
 *
 * 配置格式（在FormDefinition的formSchema中配置linkages数组）：
 * {
 *   "linkages": [
 *     {
 *       "type": "VALUE_LINKAGE",
 *       "sourceField": "price",
 *       "targetField": "totalAmount",
 *       "expression": "#price * #quantity"
 *     }
 *   ]
 * }
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldLinkageService {

    private final ExpressionEngine expressionEngine;

    /**
     * 执行字段联动计算
     * 当表单数据发生变化时调用，根据联动规则重新计算关联字段
     *
     * @param formSchema 表单Schema（含linkages配置）
     * @param formData   当前表单数据
     * @param changedField 发生变化的字段Key
     * @return 需要更新的字段Map（fieldKey -> newValue）
     */
    public Map<String, Object> executeLinkages(String formSchema, JSONObject formData, String changedField) {
        JSONObject schema = JSONObject.parseObject(formSchema);
        JSONArray linkages = schema.getJSONArray("linkages");
        if (linkages == null || linkages.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> variables = formData.toJavaObject(Map.class);
        Map<String, Object> updates = new LinkedHashMap<>();

        for (int i = 0; i < linkages.size(); i++) {
            JSONObject linkage = linkages.getJSONObject(i);
            String type = linkage.getString("type");
            String sourceField = linkage.getString("sourceField");

            // 只处理与变更字段相关的联动规则
            if (sourceField != null && !sourceField.equals(changedField)) {
                continue;
            }

            switch (type) {
                case "VALUE_LINKAGE" -> {
                    // 值联动：计算目标字段的新值
                    String targetField = linkage.getString("targetField");
                    String expression = linkage.getString("expression");
                    try {
                        Object newValue = expressionEngine.evaluate(expression, variables);
                        updates.put(targetField, newValue);
                        // 更新变量上下文（后续联动可能依赖此值）
                        variables.put(targetField, newValue);
                    } catch (Exception e) {
                        log.error("字段联动计算失败 - target: {}, expr: {}", targetField, expression, e);
                    }
                }
                case "VISIBILITY_LINKAGE" -> {
                    // 可见性联动：返回目标字段的显示/隐藏状态
                    String targetField = linkage.getString("targetField");
                    String condition = linkage.getString("condition");
                    try {
                        boolean visible = expressionEngine.evaluateBoolean(condition, variables);
                        updates.put("__visibility__." + targetField, visible);
                    } catch (Exception e) {
                        log.error("可见性联动计算失败 - target: {}", targetField, e);
                    }
                }
                case "VALIDATION_LINKAGE" -> {
                    // 校验联动：返回目标字段是否需要必填
                    String targetField = linkage.getString("targetField");
                    String condition = linkage.getString("condition");
                    try {
                        boolean required = expressionEngine.evaluateBoolean(condition, variables);
                        updates.put("__required__." + targetField, required);
                    } catch (Exception e) {
                        log.error("校验联动计算失败 - target: {}", targetField, e);
                    }
                }
                case "OPTIONS_LINKAGE" -> {
                    // 选项联动：返回目标字段的新选项列表
                    String targetField = linkage.getString("targetField");
                    String dataSource = linkage.getString("dataSource");
                    // dataSource可以是字典编码或API路径
                    updates.put("__options__." + targetField, dataSource);
                }
                default -> log.warn("未知的联动类型: {}", type);
            }
        }

        log.debug("字段联动执行完成 - 变更字段: {}, 更新项: {} 条", changedField, updates.size());
        return updates;
    }

    /**
     * 计算表单的所有初始联动值（表单加载时调用）
     */
    public Map<String, Object> executeAllLinkages(String formSchema, JSONObject formData) {
        JSONObject schema = JSONObject.parseObject(formSchema);
        JSONArray linkages = schema.getJSONArray("linkages");
        if (linkages == null || linkages.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> allUpdates = new LinkedHashMap<>();
        for (int i = 0; i < linkages.size(); i++) {
            JSONObject linkage = linkages.getJSONObject(i);
            if ("VALUE_LINKAGE".equals(linkage.getString("type"))) {
                String targetField = linkage.getString("targetField");
                allUpdates.put(targetField, null); // 标记需要计算的字段
            }
        }

        // 执行所有值联动计算
        Map<String, Object> variables = formData.toJavaObject(Map.class);
        for (String targetField : allUpdates.keySet()) {
            JSONObject linkage = findLinkageByTarget(schema, targetField);
            if (linkage != null) {
                try {
                    Object value = expressionEngine.evaluate(linkage.getString("expression"), variables);
                    allUpdates.put(targetField, value);
                    variables.put(targetField, value);
                } catch (Exception e) {
                    log.error("初始联动计算失败 - target: {}", targetField, e);
                }
            }
        }

        return allUpdates;
    }

    private JSONObject findLinkageByTarget(JSONObject schema, String targetField) {
        JSONArray linkages = schema.getJSONArray("linkages");
        if (linkages == null) return null;
        for (int i = 0; i < linkages.size(); i++) {
            JSONObject linkage = linkages.getJSONObject(i);
            if (targetField.equals(linkage.getString("targetField"))) {
                return linkage;
            }
        }
        return null;
    }
}
