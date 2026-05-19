package com.gmp.business.service.handler;

import com.gmp.common.base.ResultCode;
import com.gmp.common.constant.BusinessType;
import com.gmp.common.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务类型处理器注册中心 — 自动发现所有 BusinessTypeHandler Bean
 *
 * 使用 Spring 零配置策略注入：任何 @Component 实现了 BusinessTypeHandler 的类
 * 都会被自动收集并按 BusinessType 索引。
 *
 * @author hb176
 * @since 1.0.0
 */
@Component
public class BusinessTypeHandlerRegistry {

    private final Map<BusinessType, BusinessTypeHandler> handlerMap;

    public BusinessTypeHandlerRegistry(List<BusinessTypeHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        BusinessTypeHandler::getSupportedType,
                        Function.identity()));
    }

    /** 按业务类型获取处理器 */
    public BusinessTypeHandler getHandler(BusinessType type) {
        BusinessTypeHandler handler = handlerMap.get(type);
        if (handler == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "不支持的业务类型: " + type.getCode());
        }
        return handler;
    }

    /** 获取所有已注册的业务类型 */
    public java.util.Set<BusinessType> getRegisteredTypes() {
        return handlerMap.keySet();
    }
}
