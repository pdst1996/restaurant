package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterRoleRequest;
import com.ps.restaurant.model.Role;

import jakarta.persistence.criteria.Predicate;

public final class RoleSpecifications {

    private RoleSpecifications() {
    }

    public static Specification<Role> withFilters(FilterRoleRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.equal(
                        root.get("name"), filter.name()));
            }

            if (filter.active() != null) {
                predicates.add(cb.equal(
                        root.get("active"), filter.active()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

