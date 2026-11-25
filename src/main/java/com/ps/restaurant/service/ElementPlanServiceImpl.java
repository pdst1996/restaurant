package com.ps.restaurant.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.dto.ElementPlanRequest;
import com.ps.restaurant.dto.ElementPlanResponse;
import com.ps.restaurant.dto.ErrorResponseException.NotFoundException;
import com.ps.restaurant.dto.filters.FilterElementPlanRequest;
import com.ps.restaurant.interfaces.ElementPlanService;
import com.ps.restaurant.model.Element;
import com.ps.restaurant.repository.ElementPlanRepository;
import com.ps.restaurant.repository.ElementTypeRepository;
import com.ps.restaurant.repository.FloorRepository;
import com.ps.restaurant.repository.specification.ElementPlanSpecifications;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ElementPlanServiceImpl implements ElementPlanService {
    private final ElementPlanRepository repository;
    private final FloorRepository floorRepository;
    private final ElementTypeRepository typeRepository;

    @Override
    public List<ElementPlanResponse> filterElements(FilterElementPlanRequest filter) {
        return repository.findAll(ElementPlanSpecifications.withFilters(filter)).stream()
                .map(ElementPlanResponse::from).toList();
    }

    @Override
    public long countElements(FilterElementPlanRequest filter) {
        return repository.count(ElementPlanSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ElementPlanResponse createElement(ElementPlanRequest request) {
        Element element = new Element();
        applyRequest(element, request);
        return ElementPlanResponse.from(repository.save(element));
    }

    void applyRequest(Element element, ElementPlanRequest request) {
        element.setFloor(ServiceSupport.requireById(floorRepository, request.floorId(), "Floor"));
        element.setElementType(typeRepository.findByType(request.type())
                .orElseThrow(() -> new NotFoundException("Element type " + request.type() + " was not found")));
        element.setX(request.x());
        element.setY(request.y());
        element.setWidth(request.width());
        element.setHeight(request.height());
        if (request.rotation() != null) {
            element.setRotation(request.rotation());
        }
        element.setLabel(request.label());
    }
}
