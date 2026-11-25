package com.ps.restaurant.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ps.restaurant.dto.ElementPlanRequest;
import com.ps.restaurant.dto.ElementPlanResponse;
import com.ps.restaurant.dto.filters.FilterElementPlanRequest;
import com.ps.restaurant.interfaces.ElementPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/plan-element/plan-elements")
@RequiredArgsConstructor
public class ElementPlanController {
    private final ElementPlanService service;

    @GetMapping
    public ResponseEntity<List<ElementPlanResponse>> getElements(@Valid @ModelAttribute FilterElementPlanRequest filter) {
        return ResponseEntity.ok(service.filterElements(filter));
    }

    @PostMapping
    public ResponseEntity<ElementPlanResponse> createElement(@Valid @RequestBody ElementPlanRequest request) {
        return ResponseEntity.ok(service.createElement(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countElements(@Valid @ModelAttribute FilterElementPlanRequest filter) {
        return ResponseEntity.ok(service.countElements(filter));
    }
}
