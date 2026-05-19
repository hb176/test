package com.gmp.framework.form;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gmp.common.exceptions.BusinessException;
import com.gmp.common.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 可配置表单引擎核心服务 - 基于JSON Schema驱动动态表单
 *
 * 核心设计理念：
 * 1. 表单结构由JSON Schema定义，存储在数据库中，支持热更新
 * 2. 表单数据以JSON格式存储，灵活适应各种业务场景
 * 3. 支持丰富的字段类型：文本、数字、日期、下拉、级联、文件、表格等
 * 4. 内置校验引擎，支持必填、正则、范围、自定义规则等
 * 5. 支持表单版本管理和发布控制
 *
 * JSON Schema格式示例：
 * {
 *   "formId": "recorder_form",
 *   "formName": "记录表单",
 *   "version": 1,
 *   "fields": [
 *     {
 *       "fieldKey": "title",
 *       "fieldName": "标题",
 *       "fieldType": "TEXT",
 *       "required": true,
 *       "maxLength": 200,
 *       "placeholder": "请输入标题"
 *     }
 *   ]
 * }
 *
 * @author hb176
 * @since 1.0.0
 */
@Slf4j
public class FormEngineService {

    /** 字段类型枚举 */
    public enum FieldType {
        /** 单行文本 */
        TEXT,
        /** 多行文本(文本域) */
        TEXTAREA,
        /** 数字 */
        NUMBER,
        /** 日期 */
        DATE,
        /** 日期时间 */
        DATETIME,
        /** 下拉选择 */
        SELECT,
        /** 多选下拉 */
        MULTI_SELECT,
        /** 单选按钮 */
        RADIO,
        /** 多选框 */
        CHECKBOX,
        /** 开关 */
        SWITCH,
        /** 级联选择 */
        CASCADE,
        /** 文件上传 */
        FILE,
        /** 图片上传 */
        IMAGE,
        /** 富文本编辑器 */
        RICH_TEXT,
        /** 子表格(嵌套表单数组) */
        TABLE,
        /** 隐藏域 */
        HIDDEN,
        /** 只读展示 */
        DISPLAY
    }

    /** 校验规则类型 */
    public enum RuleType {
        /** 必填 */
        REQUIRED,
        /** 正则表达式 */
        REGEX,
        /** 数值范围 */
        RANGE,
        /** 字符串长度 */
        LENGTH,
        /** 邮箱格式 */
        EMAIL,
        /** 手机号格式 */
        PHONE,
        /** 自定义表达式 */
        CUSTOM
    }

    /**
     * 校验表单数据 - 根据表单Schema定义的校验规则验证提交的数据
     *
     * @param formSchema 表单Schema定义(JSON字符串)
     * @param formData   待校验的表单数据(JSON对象)
     * @return 校验结果：true=通过, false=不通过
     * @throws BusinessException 校验失败时抛出，消息包含失败详情
     */
    public ValidateResult validateFormData(String formSchema, JSONObject formData) {
        JSONObject schema = JSON.parseObject(formSchema);
        JSONArray fields = schema.getJSONArray("fields");
        if (fields == null || fields.isEmpty()) {
            log.warn("表单Schema中无字段定义，跳过校验");
            return ValidateResult.success();
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (int i = 0; i < fields.size(); i++) {
            JSONObject field = fields.getJSONObject(i);
            String fieldKey = field.getString("fieldKey");
            String fieldName = field.getString("fieldName");
            Object value = formData.get(fieldKey);

            // 遍历校验规则
            JSONArray rules = field.getJSONArray("rules");
            if (rules == null) {
                continue;
            }

            for (int j = 0; j < rules.size(); j++) {
                JSONObject rule = rules.getJSONObject(j);
                String ruleType = rule.getString("type");
                String ruleMessage = rule.getString("message");

                // 执行校验
                ValidationError error = executeRule(ruleType, fieldKey, fieldName, value, rule);
                if (error != null) {
                    if (rule.getBooleanValue("isWarning", false)) {
                        // 警告级别（不阻断提交）
                        warnings.add(ruleMessage != null ? ruleMessage : error.message());
                    } else {
                        // 错误级别（阻断提交）
                        errors.add(ruleMessage != null ? ruleMessage : error.message());
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            return ValidateResult.fail(String.join("; ", errors), errors);
        }
        return ValidateResult.success(warnings);
    }

    /**
     * 执行单条校验规则
     */
    private ValidationError executeRule(String ruleType, String fieldKey, String fieldName,
                                         Object value, JSONObject rule) {
        switch (ruleType) {
            case "REQUIRED":
                if (value == null || (value instanceof String && ((String) value).isBlank())) {
                    return new ValidationError(fieldKey, fieldName + "不能为空");
                }
                break;
            case "EMAIL":
                if (value != null && value instanceof String str && !str.isBlank()) {
                    if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", str)) {
                        return new ValidationError(fieldKey, fieldName + "格式不正确，请输入有效的邮箱地址");
                    }
                }
                break;
            case "PHONE":
                if (value != null && value instanceof String str && !str.isBlank()) {
                    if (!Pattern.matches("^1[3-9]\\d{9}$", str)) {
                        return new ValidationError(fieldKey, fieldName + "格式不正确，请输入有效的手机号");
                    }
                }
                break;
            case "REGEX":
                if (value != null && value instanceof String str && !str.isBlank()) {
                    String pattern = rule.getString("pattern");
                    if (pattern != null && !Pattern.matches(pattern, str)) {
                        return new ValidationError(fieldKey, fieldName + "格式不正确");
                    }
                }
                break;
            case "RANGE":
                if (value instanceof Number num) {
                    Double min = rule.getDouble("min");
                    Double max = rule.getDouble("max");
                    double val = num.doubleValue();
                    if ((min != null && val < min) || (max != null && val > max)) {
                        String rangeMsg = min != null && max != null
                                ? "取值范围: " + min + " ~ " + max
                                : min != null ? "不能小于" + min : "不能大于" + max;
                        return new ValidationError(fieldKey, fieldName + rangeMsg);
                    }
                }
                break;
            case "LENGTH":
                if (value instanceof String str) {
                    Integer minLen = rule.getInteger("min");
                    Integer maxLen = rule.getInteger("max");
                    if ((minLen != null && str.length() < minLen) ||
                            (maxLen != null && str.length() > maxLen)) {
                        String lenMsg = minLen != null && maxLen != null
                                ? "长度范围: " + minLen + " ~ " + maxLen
                                : minLen != null ? "最少" + minLen + "个字符" : "最多" + maxLen + "个字符";
                        return new ValidationError(fieldKey, fieldName + lenMsg);
                    }
                }
                break;
            case "CUSTOM":
                String customValidatorBean = rule.getString("validatorBean");
                if (customValidatorBean != null) {
                    CustomValidator validator = SpringContextHolder.getBean(
                            customValidatorBean, CustomValidator.class);
                    CustomValidator.ValidateResult result = validator.validate(
                            fieldKey, fieldName, value, rule);
                    if (!result.valid()) {
                        return new ValidationError(fieldKey, result.message());
                    }
                }
                break;
            default:
                log.warn("未知的校验规则类型: {}", ruleType);
        }
        return null; // 校验通过
    }

    /**
     * 提取表单数据中的索引字段 - 用于支持数据库查询的字段
     * 从JSON表单数据中按配置提取需要建索引的字段值
     *
     * @param formSchema    表单Schema定义
     * @param formData      表单数据JSON
     * @param indexedFields 需要索引的字段Key列表
     * @return 索引字段Map (fieldKey -> value)
     */
    public Map<String, Object> extractIndexedFields(String formSchema, JSONObject formData,
                                                     List<String> indexedFields) {
        Map<String, Object> indexed = new LinkedHashMap<>();
        if (indexedFields == null || indexedFields.isEmpty()) {
            return indexed;
        }
        for (String fieldKey : indexedFields) {
            if (formData.containsKey(fieldKey)) {
                indexed.put(fieldKey, formData.get(fieldKey));
            }
        }
        return indexed;
    }

    /**
     * 校验结果
     */
    public static class ValidateResult {
        private final boolean valid;
        private final String message;
        private final List<String> errors;
        private final List<String> warnings;

        private ValidateResult(boolean valid, String message, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.message = message;
            this.errors = errors;
            this.warnings = warnings;
        }

        public static ValidateResult success() {
            return new ValidateResult(true, "校验通过", List.of(), List.of());
        }

        public static ValidateResult success(List<String> warnings) {
            return new ValidateResult(true, "校验通过（含警告）", List.of(), warnings);
        }

        public static ValidateResult fail(String message, List<String> errors) {
            return new ValidateResult(false, message, errors, List.of());
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }

    /**
     * 校验错误详情
     */
    public record ValidationError(String fieldKey, String message) {}
}
