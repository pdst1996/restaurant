package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.ps.restaurant.dto.filters.FilterFloorRequest;
import com.ps.restaurant.model.Floor;

public final class FloorSpecifications {
    private FloorSpecifications() {

    }

    public static Specification<Floor> withFilters(FilterFloorRequest filter) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
