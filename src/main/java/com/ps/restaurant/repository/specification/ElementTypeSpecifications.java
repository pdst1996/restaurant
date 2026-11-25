package com.ps.restaurant.repository.specification;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.ps.restaurant.dto.filters.FilterElementTypeRequest;
import com.ps.restaurant.model.ElementType;
import jakarta.persistence.criteria.Predicate;

public final class ElementTypeSpecifications {
    private ElementTypeSpecifications() {
    }

    public static Specification<ElementType> withFilters(FilterElementTypeRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.id() != null) {
                predicates.add(cb.equal(root.get("id"), filter.id()));
            }
            if (filter.type() != null && !filter.type().isBlank()) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.round() != null) {
                predicates.add(cb.equal(root.get("round"), filter.round()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
