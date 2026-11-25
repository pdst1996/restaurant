package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterAllergyRequest;
import com.ps.restaurant.model.Allergy;

import jakarta.persistence.criteria.Predicate;

public final class AllergySpecifications {

    private AllergySpecifications() {
    }

    public static Specification<Allergy> withFilters(FilterAllergyRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(cb.equal(
                        root.get("id"), filter.id()));
            }

            if (filter.allergyKey() != null && !filter.allergyKey().isBlank()) {
                predicates.add(cb.equal(
                        root.get("allergyKey"), filter.allergyKey()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

