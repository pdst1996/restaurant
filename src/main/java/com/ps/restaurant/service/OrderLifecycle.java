package com.ps.restaurant.service;

import static com.ps.restaurant.config.Permissions.*;
import static com.ps.restaurant.model.OrderStatus.*;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.model.OrderStatus;

final class OrderLifecycle {
    private OrderLifecycle() {}

    static String requiredPermission(OrderStatus from, OrderStatus to) {
        if (to == CANCELLED && from != CANCELLED && from != PAID) return ORDER_CANCEL;
        if (from == DRAFT && to == OPEN) return ORDER_EDIT;
        if ((from == OPEN || from == CONFIRMED) && to == PREPARING) return ORDER_PREPARE;
        if (from == PREPARING && to == READY) return ORDER_PREPARE;
        if (from == READY && to == SERVED) return ORDER_SERVE;
        if (from == SERVED && to == PAID) return ORDER_COMPLETE;
        throw new ConflictException("Invalid order transition: " + from + " → " + to);
    }

    static boolean closed(OrderStatus status) {
        return status == PAID || status == CANCELLED;
    }
}
