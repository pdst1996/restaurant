package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterTableRequest;
import com.ps.restaurant.model.Table;

import jakarta.persistence.criteria.Predicate;

public final class TableSpecifications {

    private TableSpecifications() {
    }

    public static Specification<Table> withFilters(FilterTableRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.floorId() != null) {
                predicates.add(cb.equal(root.get("floor").get("id"), filter.floorId()));
            }
            if (filter.shape() != null) {
                predicates.add(cb.equal(root.get("shape"), filter.shape()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("tableStatus"), filter.status()));
            }

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.tableName() != null && !filter.tableName().isBlank()) {
                predicates.add(cb.equal(
                        root.get("tableName"), filter.tableName()));
            }

            if (filter.capacity() != null) {
                predicates.add(cb.equal(
                        root.get("capacity"), filter.capacity()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

