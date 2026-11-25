package com.ps.restaurant.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ps.restaurant.dto.CategoryRequest;
import com.ps.restaurant.dto.CategoryResponse;
import com.ps.restaurant.dto.filters.FilterCategoryRequest;
import com.ps.restaurant.interfaces.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/category/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @Valid @ModelAttribute FilterCategoryRequest filter) {
        return ResponseEntity.ok(categoryService.filterCategories(filter));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCategoriesCount(@Valid @ModelAttribute FilterCategoryRequest filter) {
        return ResponseEntity.ok(categoryService.countCategories(filter));
    }
}

