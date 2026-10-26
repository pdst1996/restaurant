package com.ps.restaurant.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.model.Order;

import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<Order> withFilters(
            FilterOrderRequest filter) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(
                    cb.equal(root.get("id"), filter.id())
                );
            }

            if (filter.orderNumber() != null) {
                predicates.add(
                    cb.equal(
                        root.get("orderNumber"),
                        filter.orderNumber()
                    )
                );
            }

            if (filter.status() != null) {
                predicates.add(
                    cb.equal(root.get("status"), filter.status())
                );
            }

            if (filter.tableId() != null) {
                predicates.add(
                    cb.equal(
                        root.get("table").get("id"),
                        filter.tableId()
                    )
                );
            }

            if (filter.waiterId() != null
                    && !filter.waiterId().isBlank()) {
                predicates.add(
                    cb.equal(
                        root.get("waiter").get("id"),
                        filter.waiterId()
                    )
                );
            }

            if (filter.fromDate() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filter.fromDate()
                    )
                );
            }

            if (filter.toDate() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        filter.toDate()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}