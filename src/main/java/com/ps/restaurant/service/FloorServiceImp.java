package com.ps.restaurant.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ps.restaurant.dto.ElementLayoutRequest;
import com.ps.restaurant.dto.ElementPlanResponse;
import com.ps.restaurant.dto.FloorRequest;
import com.ps.restaurant.dto.FloorResponse;
import com.ps.restaurant.dto.LayoutRequest;
import com.ps.restaurant.dto.LayoutResponse;
import com.ps.restaurant.dto.TableLayoutRequest;
import com.ps.restaurant.dto.TableResponse;
import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.filters.FilterFloorRequest;
import com.ps.restaurant.interfaces.FloorService;
import com.ps.restaurant.model.Element;
import com.ps.restaurant.model.Floor;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.repository.ElementPlanRepository;
import com.ps.restaurant.repository.FloorRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.specification.FloorSpecifications;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FloorServiceImp implements FloorService {
    private final TableRepository tableRepo;
    private final ElementPlanRepository elementRepo;
    private final FloorRepository floorRepo;
    private final TableServiceImpl tableService;
    private final ElementPlanServiceImpl elementService;

    @Override
    public List<FloorResponse> filterFloors(FilterFloorRequest filter) {
        return floorRepo.findAll(FloorSpecifications.withFilters(filter)).stream().map(FloorResponse::from).toList();
    }

    @Override
    public long countFloors(FilterFloorRequest filter) {
        return floorRepo.count(FloorSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public FloorResponse createFloor(FloorRequest request) {
        Floor floor = new Floor();
        floor.setName(request.name());
        if (request.width() != null) {
            floor.setWidth(request.width());
        }
        if (request.height() != null) {
            floor.setHeight(request.height());
        }
        if (request.sort_order() != null) {
            floor.setSort_order(request.sort_order());
        }
        return FloorResponse.from(floorRepo.save(floor));
    }

    @Override
    @Transactional
    public LayoutResponse saveLayout(LayoutRequest request) {
        Set<Long> deleted = new HashSet<>(request.deletedElementIds());
        for (Long id : deleted) {
            elementRepo.delete(ServiceSupport.requireById(elementRepo, id, "Element"));
        }
        List<TableResponse> tables = new ArrayList<>();
        Set<Long> tableIds = new HashSet<>();
        for (TableLayoutRequest dto : request.tables()) {
            if (dto.id() != null && !tableIds.add(dto.id())) {
                throw new ConflictException("A table cannot appear twice in a layout");
            }
            Table table = dto.id() == null ? new Table()
                    : ServiceSupport.requireById(tableRepo, dto.id(), "Table");
            tableService.applyRequest(table, dto.table());
            tables.add(TableResponse.from(tableRepo.save(table)));
        }
        List<ElementPlanResponse> elements = new ArrayList<>();
        Set<Long> elementIds = new HashSet<>();
        for (ElementLayoutRequest dto : request.elements()) {
            if (dto.id() != null && (deleted.contains(dto.id()) || !elementIds.add(dto.id()))) {
                throw new ConflictException("An element cannot be saved twice or deleted and saved in the same layout");
            }
            Element element = dto.id() == null ? new Element()
                    : ServiceSupport.requireById(elementRepo, dto.id(), "Element");
            elementService.applyRequest(element, dto.element());
            elements.add(ElementPlanResponse.from(elementRepo.save(element)));
        }
        tableRepo.flush();
        elementRepo.flush();
        return new LayoutResponse(tables, elements);
    }
}
