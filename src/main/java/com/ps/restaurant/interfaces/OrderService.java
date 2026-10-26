package com.ps.restaurant.interfaces;

import java.util.List;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.model.Order;

public interface OrderService {

    /**
   * Get the orders filtered
   *
   * @param filter the FilterOrderRequest filter
   * @return a list of orders filtered
   */
    public List<Order> filterOrders(FilterOrderRequest filter);
}
