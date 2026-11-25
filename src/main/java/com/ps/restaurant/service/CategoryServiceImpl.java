package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.CategoryRequest;
import com.ps.restaurant.dto.CategoryResponse;
import com.ps.restaurant.dto.filters.FilterCategoryRequest;
import com.ps.restaurant.interfaces.CategoryService;
import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.repository.CategoryRepository;
import com.ps.restaurant.repository.specification.CategorySpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    public Optional<ItemCategory> getCategoryById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<CategoryResponse> filterCategories(FilterCategoryRequest filter) {
        return repository.findAll(CategorySpecifications.withFilters(filter)).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public long countCategories(FilterCategoryRequest filter) {
        return repository.count(CategorySpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (repository.existsByCategoryKey(request.categoryKey())) {
            throw new ConflictException("Category with this categoryKey already exists");
        }

        ItemCategory entity = new ItemCategory();
        entity.setCategoryKey(request.categoryKey());
        entity.setDisplayName(request.displayName());
        return CategoryResponse.from(repository.save(entity));
    }
}

