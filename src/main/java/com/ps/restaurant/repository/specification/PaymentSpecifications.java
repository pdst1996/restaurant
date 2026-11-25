package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterPaymentRequest;
import com.ps.restaurant.model.Payment;

import jakarta.persistence.criteria.Predicate;

public final class PaymentSpecifications {

    private PaymentSpecifications() {
    }

    public static Specification<Payment> withFilters(FilterPaymentRequest filter) {
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

            if (filter.method() != null) {
                predicates.add(cb.equal(
                        root.get("method"), filter.method()));
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

