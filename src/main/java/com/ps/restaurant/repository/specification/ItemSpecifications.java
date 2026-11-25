package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterItemRequest;
import com.ps.restaurant.model.Item;

import jakarta.persistence.criteria.Predicate;

public final class ItemSpecifications {

    private ItemSpecifications(){}

     public static Specification<Item> withFilters(
            FilterItemRequest filter) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!Boolean.TRUE.equals(filter.includeInactive())) predicates.add(cb.isTrue(root.get("active")));

            if (filter.id() != null) {
                predicates.add(
                    cb.equal(root.get("id"), filter.id())
                );
            }

            if (filter.sku() != null) {
                predicates.add(
                    cb.equal(
                        root.get("sku"),
                        filter.sku()
                    )
                );
            }

            if (filter.available() != null) {
                predicates.add(
                    cb.equal(root.get("available"), filter.available())
                );
            }

            if (filter.categoryId() != null) {
                predicates.add(
                    cb.equal(
                        root.get("category").get("id"),
                        filter.categoryId()
                    )
                );
            }

            if (filter.barcode() != null && !filter.barcode().isBlank()) {
                predicates.add(
                    cb.isTrue(
                        cb.function(
                            "jsonb_contains_value",
                            Boolean.class,
                            root.get("barcodes"),
                            cb.literal(filter.barcode())
                        )
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
