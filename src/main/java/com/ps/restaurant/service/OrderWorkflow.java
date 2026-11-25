package com.ps.restaurant.service;

import static com.ps.restaurant.config.Permissions.*;
import static com.ps.restaurant.model.OrderStatus.*;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.config.SessionPrincipal;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.dto.ErrorResponseException.NotFoundException;
import com.ps.restaurant.dto.ErrorResponseException.UnauthorizedException;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.OrderBalanceResponse;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.OrderStatusChange;
import com.ps.restaurant.model.PaymentStatus;
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
    private final OrderAmounts amounts;

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
        return response(order);
    }

    @Transactional
    public OrderResponse cancel(Long id, String reason) {
        Order order = lock(id);
        apply(order, CANCELLED, reason);
        return response(order);
    }

    @Transactional
    public OrderResponse complete(Long id) {
        Order order = lock(id);
        apply(order, PAID, null);
        return response(order);
    }

    public OrderBalanceResponse balance(Long id) {
        return amounts.summarize(ServiceSupport.requireById(orders, id, "Order"));
    }

    OrderResponse response(Order order) {
        Authentication auth = authentication();
        return OrderResponse.from(order, auth == null || has(auth, PAYMENT_VIEW));
    }

    public List<OrderStatusChange> history(Long orderId) {
        if (!orders.existsById(orderId)) throw new NotFoundException("Order with id " + orderId + " was not found");
        return history.findByOrderIdOrderByIdAsc(orderId);
    }

    void created(Order order) {
        record(order, null, null);
    }

    void initializeAmounts(Order order) {
        amounts.initialize(order);
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
            throw new ErrorResponseException(409, "ORDER_REQUIRES_ITEMS", "Add at least one item before sending the order to the kitchen");
        }
        if (next == CANCELLED && (reason == null || reason.isBlank())) {
            throw new ErrorResponseException(422, "CANCEL_REASON_REQUIRED", "Cancel reason is required");
        }
        if (next == CANCELLED && order.getPayments().stream().anyMatch(
                p -> p.getStatus() == PaymentStatus.COMPLETED || p.getStatus() == PaymentStatus.PENDING)) {
            throw new ErrorResponseException(409, "ORDER_HAS_ACTIVE_PAYMENTS", "Reverse the order payments before cancelling");
        }
        if (next == PAID) {
            var balance = amounts.summarize(order);
            if (order.getItems().isEmpty() || balance.total().compareTo(balance.paid()) != 0 || balance.pending().signum() > 0) {
                throw new ErrorResponseException(409, "ORDER_NOT_FULLY_PAID", "The order must be fully paid before completion");
            }
            order.setTotal(balance.total());
        }
        order.setStatus(next);
        record(order, previous, reason);
    }

    void requireEditable(Order order) {
        if (OrderLifecycle.closed(order.getStatus())) {
            throw new ErrorResponseException(409, "ORDER_NOT_EDITABLE", "Paid or cancelled orders cannot be edited");
        }
        enforceOwnership(order.getWaiter());
    }

    void enforceOwnership(User waiter) {
        Authentication auth = authentication();
        if (auth != null && !has(auth, ORDER_ASSIGN)
                && SessionPrincipal.userId(auth).filter(waiter.getId()::equals).isEmpty()) {
            throw new AccessDeniedException("This order belongs to another waiter");
        }
    }

    void reopenAfterRefund(Order order, String reason) {
        if (order.getStatus() == PAID && amounts.summarize(order).balance().signum() > 0) {
            order.setStatus(SERVED);
            record(order, PAID, reason);
        }
    }

    void updateTotal(Order order) {
        var balance = amounts.recalculate(order);
        if (balance.total().compareTo(balance.paid().add(balance.pending())) < 0) {
            throw new ErrorResponseException(409, "ORDER_TOTAL_BELOW_PAYMENTS", "The order total cannot be lower than its payments");
        }
        order.setTotal(balance.total());
    }

    User actor() {
        Authentication auth = authentication();
        if (auth == null) return null; // Internal calls and pre-RBAC import jobs need no actor.
        return SessionPrincipal.userId(auth).flatMap(users::findById)
                .orElseThrow(() -> new UnauthorizedException("Invalid session"));
    }

    User requireActor() {
        User user = actor();
        if (user == null) throw new UnauthorizedException("Authentication required");
        return user;
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
