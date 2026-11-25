package com.ps.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ps.restaurant.dto.AllergyRequest;
import com.ps.restaurant.dto.AllergyResponse;
import com.ps.restaurant.dto.CategoryRequest;
import com.ps.restaurant.dto.CategoryResponse;
import com.ps.restaurant.dto.ItemAttributeRequest;
import com.ps.restaurant.dto.ItemAttributeResponse;
import com.ps.restaurant.dto.ItemRequest;
import com.ps.restaurant.dto.ItemResponse;
import com.ps.restaurant.dto.ItemSizeRequest;
import com.ps.restaurant.dto.ItemSizeResponse;
import com.ps.restaurant.dto.OrderItemRequest;
import com.ps.restaurant.dto.OrderItemResponse;
import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.PaymentRequest;
import com.ps.restaurant.dto.PaymentResponse;
import com.ps.restaurant.dto.RoleRequest;
import com.ps.restaurant.dto.RoleResponse;
import com.ps.restaurant.dto.TableRequest;
import com.ps.restaurant.dto.TableResponse;
import com.ps.restaurant.dto.FloorRequest;
import com.ps.restaurant.dto.FloorResponse;
import com.ps.restaurant.dto.ElementPlanRequest;
import com.ps.restaurant.dto.ElementPlanResponse;
import com.ps.restaurant.dto.ElementTypeRequest;
import com.ps.restaurant.dto.ElementTypeResponse;
import com.ps.restaurant.dto.LayoutRequest;
import com.ps.restaurant.dto.LayoutResponse;
import com.ps.restaurant.dto.RoleReferenceRequest;
import com.ps.restaurant.dto.UserRequest;
import com.ps.restaurant.dto.UserResponse;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();

        for (Class<?> dto : new Class<?>[] {
                AllergyRequest.class, AllergyResponse.class,
                CategoryRequest.class, CategoryResponse.class,
                FloorRequest.class, FloorResponse.class,
                ElementPlanRequest.class, ElementPlanResponse.class,
                ElementTypeRequest.class, ElementTypeResponse.class,
                LayoutRequest.class, LayoutResponse.class,
                ItemAttributeRequest.class, ItemAttributeResponse.class,
                ItemRequest.class, ItemResponse.class,
                ItemSizeRequest.class, ItemSizeResponse.class,
                OrderItemRequest.class, OrderItemResponse.class,
                OrderRequest.class, OrderResponse.class,
                PaymentRequest.class, PaymentResponse.class,
                RoleRequest.class, RoleResponse.class,
                RoleReferenceRequest.class, UserRequest.class, UserResponse.class,
                TableRequest.class, TableResponse.class}) {
            var resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(dto));
            components.addSchemas(dto.getSimpleName(), resolved.schema);
            resolved.referencedSchemas.forEach(components::addSchemas);
        }

        return new OpenAPI().components(components);
    }
}
