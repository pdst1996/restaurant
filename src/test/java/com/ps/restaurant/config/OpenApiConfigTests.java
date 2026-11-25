package com.ps.restaurant.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.media.Schema;

class OpenApiConfigTests {

    @Test
    void registersCurrentCatalogDtosAndCorrectTableRequest() {
        var schemas = new OpenApiConfig().customOpenAPI().getComponents().getSchemas();

        assertThat(schemas).containsKeys(
                "AllergyRequest", "AllergyResponse", "CategoryRequest", "CategoryResponse",
                "ItemAttributeRequest", "ItemAttributeResponse", "ItemSizeRequest", "ItemSizeResponse",
                "ItemRequest", "ItemResponse", "OrderRequest", "OrderResponse",
                "OrderItemRequest", "OrderItemResponse", "PaymentRequest", "PaymentResponse",
                "RoleRequest", "RoleResponse", "TableRequest", "TableResponse",
                "FloorRequest", "FloorResponse", "ElementTypeRequest", "ElementTypeResponse",
                "ElementPlanRequest", "ElementPlanResponse", "LayoutRequest", "LayoutResponse",
                "TableLayoutRequest", "ElementLayoutRequest", "UserRequest", "UserResponse", "RoleReferenceRequest");
        assertThat(schemas).doesNotContainKeys("ProductRequest", "ProductResponse", "Allergy", "ItemSize", "Role", "User");
        assertThat(schemas.get("TableRequest").getProperties()).containsKeys("tableName", "capacity", "floorId", "shape");
        assertThat(schemas.get("CategoryRequest").getProperties()).containsKeys("categoryKey", "displayName");
        assertThat(schemas.get("ItemRequest").getProperties())
                .containsKeys("displayName", "barcodes", "sizeIds", "attributeIds");
        assertThat(schemas.get("OrderItemRequest").getProperties()).containsKeys("itemId", "sizeId");
    }

    @Test
    void resolvesNestedResponseDtoReferences() {
        var schemas = new OpenApiConfig().customOpenAPI().getComponents().getSchemas();

        Schema<?> allergies = (Schema<?>) schemas.get("OrderResponse").getProperties().get("allergies");
        assertThat(allergies.getItems().get$ref()).isEqualTo("#/components/schemas/AllergyResponse");
        Schema<?> size = (Schema<?>) schemas.get("OrderItemResponse").getProperties().get("size");
        assertThat(size.get$ref()).isEqualTo("#/components/schemas/ItemSizeResponse");
    }
}
