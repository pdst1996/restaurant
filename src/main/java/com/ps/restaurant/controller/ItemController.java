package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.ItemRequest;
import com.ps.restaurant.dto.ItemResponse;
import com.ps.restaurant.dto.filters.FilterItemRequest;
import com.ps.restaurant.interfaces.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/item/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems(
            @Valid @ModelAttribute FilterItemRequest filter) {
        return ResponseEntity.ok(itemService.filterItems(filter));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.createItem(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getItemsCount(@Valid @ModelAttribute FilterItemRequest filter) {
        return ResponseEntity.ok(itemService.countItems(filter));
    }
}

