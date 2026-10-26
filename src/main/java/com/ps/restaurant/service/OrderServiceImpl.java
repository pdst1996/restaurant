package com.ps.restaurant.service;

import java.util.List;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.interfaces.OrderService;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.specification.OrderSpecifications;

public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;

    public List<Order> filterOrders(FilterOrderRequest filter) {

        validateDateRange(filter);

        return orderRepository.findAll(
            OrderSpecifications.withFilters(filter)
        );
    }

    private void validateDateRange(FilterOrderRequest filter) {
        if (filter.fromDate() != null
                && filter.toDate() != null
                && filter.fromDate().isAfter(filter.toDate())) {
            throw new IllegalArgumentException(
                "fromDate must be before or equal to toDate"
            );
        }
    }
}
