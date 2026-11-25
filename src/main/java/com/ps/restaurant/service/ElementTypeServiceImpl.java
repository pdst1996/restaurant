package com.ps.restaurant.service;

import com.ps.restaurant.interfaces.ElementTypeService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.dto.ElementTypeRequest;
import com.ps.restaurant.dto.ElementTypeResponse;
import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.filters.FilterElementTypeRequest;
import com.ps.restaurant.model.ElementType;
import com.ps.restaurant.repository.ElementTypeRepository;
import com.ps.restaurant.repository.specification.ElementTypeSpecifications;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElementTypeServiceImpl implements ElementTypeService {
    private final ElementTypeRepository repository;

    @Override
    public List<ElementTypeResponse> filterElementTypes(FilterElementTypeRequest filter) {
        return repository.findAll(ElementTypeSpecifications.withFilters(filter)).stream()
                .map(ElementTypeResponse::from).toList();
    }

    @Override
    public long countElementTypes(FilterElementTypeRequest filter) {
        return repository.count(ElementTypeSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ElementTypeResponse createElementType(ElementTypeRequest request) {
        if (repository.existsByType(request.type())) {
            throw new ConflictException("Element type already exists");
        }
        ElementType type = new ElementType();
        type.setType(request.type());
        type.setLabel(request.label());
        type.setWidth(request.width());
        type.setHeight(request.height());
        type.setRound(request.round());
        type.setFill(request.fill());
        type.setStroke(request.stroke());
        return ElementTypeResponse.from(repository.save(type));
    }
}
