package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.interfaces.OrderService;
import com.ps.restaurant.dto.OrderTransitionRequest;
import com.ps.restaurant.dto.OperationReasonRequest;
import com.ps.restaurant.dto.OrderBalanceResponse;
import com.ps.restaurant.model.OrderStatusChange;
import com.ps.restaurant.service.OrderWorkflow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/order/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final OrderWorkflow workflow;

    @GetMapping("/{id}/balance")
    public OrderBalanceResponse balance(@PathVariable Long id) {
        return workflow.balance(id);
    }

    @PostMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @Valid @RequestBody OrderTransitionRequest request) {
        return workflow.change(id, request.status());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id, @Valid @RequestBody OperationReasonRequest request) {
        return workflow.cancel(id, request.reason());
    }

    @PostMapping("/{id}/complete")
    public OrderResponse complete(@PathVariable Long id) {
        return workflow.complete(id);
    }

    @GetMapping("/{id}/history")
    public List<OrderStatusChange> history(@PathVariable Long id) {
        return workflow.history(id);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@Valid @ModelAttribute FilterOrderRequest filter) {
        return ResponseEntity.ok(orderService.filterOrders(filter));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getOrdersCount(@Valid @ModelAttribute FilterOrderRequest filter) {
        return ResponseEntity.ok(orderService.countOrders(filter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder( @PathVariable Long id, @Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderRequest));
    }

}
