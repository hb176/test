package com.gmp.common.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

class BusinessTypeTest {

    @ParameterizedTest
    @EnumSource(BusinessType.class)
    void everyTypeShouldHaveUniqueCode(BusinessType type) {
        assertThat(type.getCode()).isNotBlank();
        assertThat(type.getDisplayName()).isNotBlank();
        assertThat(type.getNoPrefix()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(BusinessType.class)
    void fromCodeShouldRoundTrip(BusinessType type) {
        assertThat(BusinessType.fromCode(type.getCode())).isEqualTo(type);
    }

    @Test
    void fromCodeShouldThrowOnInvalidCode() {
        assertThatThrownBy(() -> BusinessType.fromCode("INVALID_TYPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID_TYPE");
    }

    @Test
    void qmsDeviationShouldHaveCorrectPrefix() {
        assertThat(BusinessType.QMS_DEVIATION.getNoPrefix()).isEqualTo("DEV");
        assertThat(BusinessType.QMS_CAPA.getNoPrefix()).isEqualTo("CAPA");
        assertThat(BusinessType.QMS_CHANGE.getNoPrefix()).isEqualTo("CC");
    }

    @Test
    void dmsDocumentShouldHaveDocPrefix() {
        assertThat(BusinessType.DMS_DOCUMENT.getNoPrefix()).isEqualTo("DOC");
    }

    @Test
    void countShouldBe15() {
        assertThat(BusinessType.values()).hasSize(15);
    }
}
