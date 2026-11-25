package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.model.Order;

public interface OrderService {

    /**
     * Returns the order with the given identifier, or empty if it does not exist.
     */
    Optional<Order> getOrderById(Long id);

    /**
     * Get the orders filtered
     *
     * @param filter the FilterOrderRequest filter
     * @return a list of orders filtered
     */
    public List<OrderResponse> filterOrders(FilterOrderRequest filter);

    long countOrders(FilterOrderRequest filter);

    /**
     * Save the order
     *
     * @param orderRequest
     * @return the order created
     */
    public OrderResponse createOrder(OrderRequest orderRequest);

    /**
     *  Update the order
     *
     * @param orderRequest
     * @return the order created
     */
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest);
}
