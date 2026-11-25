package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterItemSizeRequest;
import com.ps.restaurant.model.ItemSize;

import jakarta.persistence.criteria.Predicate;

public final class ItemSizeSpecifications {

    private ItemSizeSpecifications() {
    }

    public static Specification<ItemSize> withFilters(FilterItemSizeRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.sizeKey() != null && !filter.sizeKey().isBlank()) {
                predicates.add(cb.equal(
                        root.get("sizeKey"), filter.sizeKey()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

