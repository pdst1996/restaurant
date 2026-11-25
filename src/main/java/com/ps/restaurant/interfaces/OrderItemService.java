package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.OrderItemRequest;
import com.ps.restaurant.dto.OrderItemResponse;
import com.ps.restaurant.dto.filters.FilterOrderItemRequest;
import com.ps.restaurant.model.OrderItem;

public interface OrderItemService {

    Optional<OrderItem> getOrderItemById(Long id);

    List<OrderItemResponse> filterOrderItems(FilterOrderItemRequest filter);

    long countOrderItems(FilterOrderItemRequest filter);

    OrderItemResponse createOrderItem(OrderItemRequest request);

    ApiResponse<String> deleteOrderItem(Long request);
}

