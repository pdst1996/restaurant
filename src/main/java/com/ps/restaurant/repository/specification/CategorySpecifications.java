package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterCategoryRequest;
import com.ps.restaurant.model.ItemCategory;

import jakarta.persistence.criteria.Predicate;

public final class CategorySpecifications {

    private CategorySpecifications() {
    }

    public static Specification<ItemCategory> withFilters(FilterCategoryRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.categoryKey() != null && !filter.categoryKey().isBlank()) {
                predicates.add(cb.equal(
                        root.get("categoryKey"), filter.categoryKey()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

