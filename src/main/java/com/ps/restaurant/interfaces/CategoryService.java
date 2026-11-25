package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.CategoryRequest;
import com.ps.restaurant.dto.CategoryResponse;
import com.ps.restaurant.dto.filters.FilterCategoryRequest;
import com.ps.restaurant.model.ItemCategory;

public interface CategoryService {

    Optional<ItemCategory> getCategoryById(Long id);

    List<CategoryResponse> filterCategories(FilterCategoryRequest filter);

    long countCategories(FilterCategoryRequest filter);

    CategoryResponse createCategory(CategoryRequest request);
}

