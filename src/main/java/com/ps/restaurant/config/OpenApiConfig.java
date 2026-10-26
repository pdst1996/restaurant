package com.ps.restaurant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ps.restaurant.dto.CategoryRequest;
import com.ps.restaurant.dto.CategoryResponse;
import com.ps.restaurant.dto.OrderItemRequest;
import com.ps.restaurant.dto.OrderItemResponse;
import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.ProductRequest;
import com.ps.restaurant.dto.RoleResponse;
import com.ps.restaurant.dto.TableResponse;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();

        components.addSchemas(
            "CategoryRequest",
            resolveSchema(CategoryRequest.class)
        );

        components.addSchemas(
            "CategoryResponse",
            resolveSchema(CategoryResponse.class)
        );

        components.addSchemas(
            "OrderItemRequest",
            resolveSchema(OrderItemRequest.class)
        );

        components.addSchemas(
            "OrderItemResponse",
            resolveSchema(OrderItemResponse.class)
        );

        components.addSchemas(
            "OrderRequest",
            resolveSchema(OrderRequest.class)
        );

        components.addSchemas(
            "OrderResponse",
            resolveSchema(OrderResponse.class)
        );

        components.addSchemas(
            "PaymentRequest",
            resolveSchema(PaymentRequest.class)
        );

        components.addSchemas(
            "PaymentResponse",
            resolveSchema(PaymentResponse.class)
        );

        components.addSchemas(
            "ProductRequest",
            resolveSchema(ProductRequest.class)
        );

        components.addSchemas(
            "TableRequest",
            resolveSchema(RoleResponse.class)
        );

        components.addSchemas(
            "TableResponse",
            resolveSchema(TableResponse.class)
        );

        return new OpenAPI().components(components);
    }

    private Schema<?> resolveSchema(Class<?> modelClass) {
        return io.swagger.v3.core.converter.ModelConverters
            .getInstance()
            .resolveAsResolvedSchema(
                new io.swagger.v3.core.converter.AnnotatedType(modelClass)
            )
            .schema;
    }
}