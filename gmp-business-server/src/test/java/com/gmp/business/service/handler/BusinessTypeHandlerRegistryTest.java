package com.gmp.business.service.handler;

import com.gmp.business.dto.CreateBusinessRequest;
import com.gmp.business.entity.BusinessRecord;
import com.gmp.common.constant.BusinessType;
import com.gmp.common.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BusinessTypeHandlerRegistryTest {

    private BusinessTypeHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        List<BusinessTypeHandler> handlers = List.of(
                new StubHandler(BusinessType.QMS_DEVIATION),
                new StubHandler(BusinessType.DMS_DOCUMENT),
                new StubHandler(BusinessType.TMS_COURSE)
        );
        registry = new BusinessTypeHandlerRegistry(handlers);
    }

    @Test
    void shouldReturnCorrectHandlerForRegisteredType() {
        BusinessTypeHandler handler = registry.getHandler(BusinessType.QMS_DEVIATION);
        assertThat(handler.getSupportedType()).isEqualTo(BusinessType.QMS_DEVIATION);
    }

    @Test
    void shouldThrowWhenTypeNotRegistered() {
        assertThatThrownBy(() -> registry.getHandler(BusinessType.QMS_RISK))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的业务类型");
    }

    @Test
    void shouldReturnAllRegisteredTypes() {
        assertThat(registry.getRegisteredTypes())
                .containsExactlyInAnyOrder(
                        BusinessType.QMS_DEVIATION,
                        BusinessType.DMS_DOCUMENT,
                        BusinessType.TMS_COURSE);
    }

    @Test
    void shouldWorkWithHandlersFromDifferentDomains() {
        // DMS handler should be reachable
        BusinessTypeHandler dms = registry.getHandler(BusinessType.DMS_DOCUMENT);
        assertThat(dms.getSupportedType().getNoPrefix()).isEqualTo("DOC");

        // TMS handler should be reachable
        BusinessTypeHandler tms = registry.getHandler(BusinessType.TMS_COURSE);
        assertThat(tms.getSupportedType().getNoPrefix()).isEqualTo("CRS");
    }

    /** 用于测试的 Stub Handler */
    static class StubHandler implements BusinessTypeHandler {
        private final BusinessType type;

        StubHandler(BusinessType type) { this.type = type; }

        @Override public BusinessType getSupportedType() { return type; }

        @Override
        public String generateBusinessNo(BusinessRecord record) {
            return type.getNoPrefix() + "-TEST-001";
        }

        @Override
        public void validate(CreateBusinessRequest request) {
            if (request.getTitle() == null) {
                throw new BusinessException(400, "标题不能为空");
            }
        }
    }
}
