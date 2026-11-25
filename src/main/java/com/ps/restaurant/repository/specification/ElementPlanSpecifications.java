package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterElementPlanRequest;
import com.ps.restaurant.model.Element;

import jakarta.persistence.criteria.Predicate;

public final class ElementPlanSpecifications {

    private ElementPlanSpecifications() {
    }

    public static Specification<Element> withFilters(FilterElementPlanRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.floorId() != null) {
                predicates.add(cb.equal(root.get("floor").get("id"), filter.floorId()));
            }

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.label() != null && !filter.label().isBlank()) {
                predicates.add(cb.equal(
                        root.get("label"), filter.label()));
            }

            if (filter.type() != null && !filter.type().isBlank()) {
                predicates.add(cb.equal(
                        root.get("elementType").get("type"), filter.type()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

