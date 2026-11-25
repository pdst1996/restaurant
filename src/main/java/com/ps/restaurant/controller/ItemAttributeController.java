package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.ItemAttributeRequest;
import com.ps.restaurant.dto.ItemAttributeResponse;
import com.ps.restaurant.dto.filters.FilterItemAttributeRequest;
import com.ps.restaurant.interfaces.ItemAttributeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/item-attribute/item-attributes")
@RequiredArgsConstructor
public class ItemAttributeController {

    private final ItemAttributeService itemAttributeService;

    @GetMapping
    public ResponseEntity<List<ItemAttributeResponse>> getItemAttributes(
            @Valid @ModelAttribute FilterItemAttributeRequest filter) {
        return ResponseEntity.ok(itemAttributeService.filterItemAttributes(filter));
    }

    @PostMapping
    public ResponseEntity<ItemAttributeResponse> createItemAttribute(@Valid @RequestBody ItemAttributeRequest request) {
        return ResponseEntity.ok(itemAttributeService.createItemAttribute(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getItemAttributesCount(@Valid @ModelAttribute FilterItemAttributeRequest filter) {
        return ResponseEntity.ok(itemAttributeService.countItemAttributes(filter));
    }
}

