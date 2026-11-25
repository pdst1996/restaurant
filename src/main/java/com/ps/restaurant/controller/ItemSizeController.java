package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.ItemSizeRequest;
import com.ps.restaurant.dto.ItemSizeResponse;
import com.ps.restaurant.dto.filters.FilterItemSizeRequest;
import com.ps.restaurant.interfaces.ItemSizeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/item-size/item-sizes")
@RequiredArgsConstructor
public class ItemSizeController {

    private final ItemSizeService itemSizeService;

    @GetMapping
    public ResponseEntity<List<ItemSizeResponse>> getItemSizes(
            @Valid @ModelAttribute FilterItemSizeRequest filter) {
        return ResponseEntity.ok(itemSizeService.filterItemSizes(filter));
    }

    @PostMapping
    public ResponseEntity<ItemSizeResponse> createItemSize(@Valid @RequestBody ItemSizeRequest request) {
        return ResponseEntity.ok(itemSizeService.createItemSize(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getItemSizesCount(@Valid @ModelAttribute FilterItemSizeRequest filter) {
        return ResponseEntity.ok(itemSizeService.countItemSizes(filter));
    }
}

