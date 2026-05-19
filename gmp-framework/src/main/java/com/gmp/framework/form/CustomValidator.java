package com.gmp.framework.form;

import com.alibaba.fastjson2.JSONObject;

/**
 * 自定义表单校验器 SPI — 供业务层实现
 *
 * 实现此接口并注册为 Spring Bean（bean name 需与表单 Schema 中 CUSTOM 规则的
 * validatorBean 字段一致），FormEngineService 会在执行 CUSTOM 规则时自动调用。
 *
 * @author hb176
 * @since 1.0.0
 */
public interface CustomValidator {

    /** 自定义校验结果 */
    record ValidateResult(boolean valid, String message) {
        public static ValidateResult ok() {
            return new ValidateResult(true, null);
        }

        public static ValidateResult fail(String message) {
            return new ValidateResult(false, message);
        }
    }

    /**
     * 执行自定义校验
     *
     * @param fieldKey   字段 Key
     * @param fieldName  字段名称
     * @param value      字段值（可能为 null）
     * @param ruleConfig CUSTOM 规则的完整 JSON 配置（可读取 validatorBean 以外的自定义参数）
     * @return 校验结果
     */
    ValidateResult validate(String fieldKey, String fieldName, Object value, JSONObject ruleConfig);
}
