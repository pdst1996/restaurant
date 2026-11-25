package com.ps.restaurant.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.Validation;
import tools.jackson.databind.json.JsonMapper;

class OrderRequestTests {

    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"togo\":null"})
    void defaultsMissingOrNullTogoToFalse(String togo) {
        OrderRequest order = JSON.readValue(
                "{\"orderNumber\":\"SEED-ORD-232344\",\"waiterId\":1" + togo + "}", OrderRequest.class);

        assertThat(order.togo()).isFalse();
        assertThat(order.tableId()).isNull();
        assertThat(order.status()).isNull();
        assertThat(order.total()).isNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(order)).isEmpty();
        }
    }
}
