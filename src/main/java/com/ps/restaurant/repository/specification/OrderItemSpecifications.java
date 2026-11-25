package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterOrderItemRequest;
import com.ps.restaurant.model.OrderItem;

import jakarta.persistence.criteria.Predicate;

public final class OrderItemSpecifications {

    private OrderItemSpecifications() {
    }

    public static Specification<OrderItem> withFilters(FilterOrderItemRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.orderId() != null) {
                predicates.add(cb.equal(
                        root.get("order").get("id"), filter.orderId()));
            }

            if (filter.itemId() != null) {
                predicates.add(cb.equal(
                        root.get("item").get("id"), filter.itemId()));
            }

            if (filter.sizeId() != null) {
                predicates.add(cb.equal(
                        root.get("size").get("id"), filter.sizeId()));
            }

            if (filter.quantity() != null) {
                predicates.add(cb.equal(
                        root.get("quantity"), filter.quantity()));
            }

            if (filter.fromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), filter.fromDate()));
            }

            if (filter.toDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), filter.toDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

