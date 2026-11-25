package com.ps.restaurant.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ps.restaurant.dto.ElementTypeRequest;
import com.ps.restaurant.dto.ElementTypeResponse;
import com.ps.restaurant.dto.filters.FilterElementTypeRequest;
import com.ps.restaurant.interfaces.ElementTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/element-type/element-types")
@RequiredArgsConstructor
public class ElementTypeController {
    private final ElementTypeService service;

    @GetMapping
    public ResponseEntity<List<ElementTypeResponse>> getElementTypes(@Valid @ModelAttribute FilterElementTypeRequest filter) {
        return ResponseEntity.ok(service.filterElementTypes(filter));
    }

    @PostMapping
    public ResponseEntity<ElementTypeResponse> createElementType(@Valid @RequestBody ElementTypeRequest request) {
        return ResponseEntity.ok(service.createElementType(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countElementTypes(@Valid @ModelAttribute FilterElementTypeRequest filter) {
        return ResponseEntity.ok(service.countElementTypes(filter));
    }
}
