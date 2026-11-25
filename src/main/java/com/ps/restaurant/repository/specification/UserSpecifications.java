package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterUserRequest;
import com.ps.restaurant.model.User;

import jakarta.persistence.criteria.Predicate;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withFilters(FilterUserRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(cb.equal(
                        root.get("email"), filter.email()));
            }

            if (filter.user() != null && !filter.user().isBlank()) {
                predicates.add(cb.equal(
                        root.get("user"), filter.user()));
            }

            if (filter.fullName() != null && !filter.fullName().isBlank()) {
                predicates.add(cb.equal(
                        root.get("fullName"), filter.fullName()));
            }

            if (filter.roleId() != null) {
                predicates.add(cb.equal(
                        root.get("role").get("id"), filter.roleId()));
            }

            if (filter.active() != null) {
                predicates.add(cb.equal(
                        root.get("active"), filter.active()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

