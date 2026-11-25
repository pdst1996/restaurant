package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterItemAttributeRequest;
import com.ps.restaurant.model.ItemAttribute;

import jakarta.persistence.criteria.Predicate;

public final class ItemAttributeSpecifications {

    private ItemAttributeSpecifications() {
    }

    public static Specification<ItemAttribute> withFilters(FilterItemAttributeRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.attributeKey() != null && !filter.attributeKey().isBlank()) {
                predicates.add(cb.equal(
                        root.get("attributeKey"), filter.attributeKey()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

