package com.ps.restaurant.service;

import static com.ps.restaurant.config.Permissions.*;
import static com.ps.restaurant.model.OrderStatus.*;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ErrorResponseException.NotFoundException;
import com.ps.restaurant.dto.ErrorResponseException.UnauthorizedException;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.OrderStatusChange;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.OrderStatusHistoryRepository;
import com.ps.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderWorkflow {
    private final OrderRepository orders;
    private final OrderStatusHistoryRepository history;
    private final UserRepository users;

    public Order lock(Long id) {
        return orders.lockById(id).orElseThrow(() -> new NotFoundException("Order with id " + id + " was not found"));
    }

    @Transactional
    public OrderResponse change(Long id, OrderStatus next) {
        if (next == PAID || next == CANCELLED) {
            throw new ErrorResponseException(422, "USE_ORDER_ACTION", "Use the complete or cancel action");
        }
        Order order = lock(id);
        apply(order, next, null);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(Long id, String reason) {
        Order order = lock(id);
        apply(order, CANCELLED, reason);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse complete(Long id) {
        Order order = lock(id);
        apply(order, PAID, null);
        return OrderResponse.from(order);
    }

    public List<OrderStatusChange> history(Long orderId) {
        if (!orders.existsById(orderId)) throw new NotFoundException("Order with id " + orderId + " was not found");
        return history.findByOrderIdOrderByIdAsc(orderId);
    }

    void created(Order order) {
        record(order, null, null);
    }

    void apply(Order order, OrderStatus next, String reason) {
        OrderStatus previous = order.getStatus();
        String permission = OrderLifecycle.requiredPermission(previous, next);
        Authentication auth = authentication();
        if (auth != null && (!has(auth, permission)
                || (next != CANCELLED && next != PAID && !has(auth, ORDER_STATUS)))) {
            throw new AccessDeniedException("Permission denied for order transition");
        }
        if ((next == OPEN || next == SERVED || next == CANCELLED) && auth != null) enforceOwnership(order.getWaiter());
        if ((next == OPEN || next == PREPARING) && order.getItems().isEmpty()) {
            throw new ConflictException("An order needs at least one item before sending it to the kitchen");
        }
        if (next == CANCELLED && (reason == null || reason.isBlank())) {
            throw new ErrorResponseException(422, "CANCEL_REASON_REQUIRED", "Cancel reason is required");
        }
        if (next == CANCELLED && !order.getPayments().isEmpty()) {
            throw new ConflictException("Reverse the order payments before cancelling");
        }
        if (next == PAID && (order.getItems().isEmpty() || order.getTotal().compareTo(recordedAmount(order)) != 0)) {
            throw new ConflictException("The order must be fully paid before completion");
        }
        order.setStatus(next);
        record(order, previous, reason);
    }

    void requireEditable(Order order) {
        if (OrderLifecycle.closed(order.getStatus())) throw new ConflictException("Paid or cancelled orders cannot be edited");
        enforceOwnership(order.getWaiter());
    }

    void enforceOwnership(User waiter) {
        Authentication auth = authentication();
        if (auth != null && !has(auth, ORDER_ASSIGN) && !auth.getName().equals(waiter.getEmail())) {
            throw new AccessDeniedException("This order belongs to another waiter");
        }
    }

    BigDecimal recordedAmount(Order order) {
        return order.getPayments().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void updateTotal(Order order) {
        BigDecimal total = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(recordedAmount(order)) < 0) throw new ConflictException("The order total cannot be lower than its payments");
        order.setTotal(total);
    }

    User actor() {
        Authentication auth = authentication();
        if (auth == null) return null; // Internal calls and pre-RBAC import jobs need no actor.
        return users.findByEmail(auth.getName()).orElseThrow(() -> new UnauthorizedException("Invalid session"));
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private void record(Order order, OrderStatus previous, String reason) {
        User user = actor();
        history.save(new OrderStatusChange(order.getId(), user == null ? null : user.getId(),
                previous, order.getStatus(), reason == null ? null : reason.strip()));
    }
}
