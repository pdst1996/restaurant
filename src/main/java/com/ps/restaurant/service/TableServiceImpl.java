package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.TableRequest;
import com.ps.restaurant.dto.TableResponse;
import com.ps.restaurant.dto.filters.FilterTableRequest;
import com.ps.restaurant.interfaces.TableService;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.FloorRepository;
import com.ps.restaurant.repository.specification.TableSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;
    private final FloorRepository floorRepository;

    @Override
    public Optional<Table> getTableById(Long id) {
        return tableRepository.findById(id);
    }

    @Override
    public List<TableResponse> filterTables(FilterTableRequest filter) {
        return tableRepository.findAll(TableSpecifications.withFilters(filter)).stream()
                .map(TableResponse::from)
                .toList();
    }

    @Override
    public long countTables(FilterTableRequest filter) {
        return tableRepository.count(TableSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public TableResponse createTable(TableRequest tableRequest) {
        if (tableRepository.existsByTableName(tableRequest.tableName())) {
            throw new ConflictException("Table with this tableName already exists");
        }
        Table table = new Table();
        applyRequest(table, tableRequest);
        return TableResponse.from(tableRepository.save(table));
    }

    void applyRequest(Table table, TableRequest request) {
        table.setFloor(ServiceSupport.requireById(floorRepository, request.floorId(), "Floor"));
        table.setTableName(request.tableName());
        table.setCapacity(request.capacity());
        table.setX(request.x());
        table.setY(request.y());
        if (request.width() != null) {
            table.setWidth(request.width());
        }
        if (request.height() != null) {
            table.setHeight(request.height());
        }
        if (request.rotation() != null) {
            table.setRotation(request.rotation());
        }
        if (request.shape() != null) {
            table.setShape(request.shape());
        }
    }
}
