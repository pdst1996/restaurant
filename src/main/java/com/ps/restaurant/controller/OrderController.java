package com.ps.restaurant.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.interfaces.OrderService;
import com.ps.restaurant.model.Order;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping("/order")
public class OrderController {

    private OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@Valid @ModelAttribute FilterOrderRequest filter) {
        List<OrderResponse> filteredOrders = orderService
            .filterOrders(filter)
            .stream()
            .map(OrderResponse::from)
            .toList();
        return ResponseEntity.ok(filteredOrders);
    }
    

}
