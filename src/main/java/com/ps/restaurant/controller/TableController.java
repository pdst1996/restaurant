package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.TableRequest;
import com.ps.restaurant.dto.TableResponse;
import com.ps.restaurant.dto.filters.FilterTableRequest;
import com.ps.restaurant.interfaces.TableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/table/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableResponse>> getTables(@Valid @ModelAttribute FilterTableRequest filter) {
        return ResponseEntity.ok(tableService.filterTables(filter));
    }

    @PostMapping
    public ResponseEntity<TableResponse> createTable(@Valid @RequestBody TableRequest tableRequest) {
        return ResponseEntity.ok(tableService.createTable(tableRequest));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTablesCount(@Valid @ModelAttribute FilterTableRequest filter) {
        return ResponseEntity.ok(tableService.countTables(filter));
    }


}
