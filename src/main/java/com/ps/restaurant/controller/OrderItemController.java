package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.OrderItemRequest;
import com.ps.restaurant.dto.OrderItemResponse;
import com.ps.restaurant.dto.filters.FilterOrderItemRequest;
import com.ps.restaurant.interfaces.OrderItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order-item/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(
            @Valid @ModelAttribute FilterOrderItemRequest filter) {
        return ResponseEntity.ok(orderItemService.filterOrderItems(filter));
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> createOrderItem(@Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderItemService.createOrderItem(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getOrderItemsCount(@Valid @ModelAttribute FilterOrderItemRequest filter) {
        return ResponseEntity.ok(orderItemService.countOrderItems(filter));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrderItem(@PathVariable Long id) {
        return orderItemService.deleteOrderItem(id);
    }
}

