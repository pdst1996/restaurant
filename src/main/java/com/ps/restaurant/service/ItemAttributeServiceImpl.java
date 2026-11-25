package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ItemAttributeRequest;
import com.ps.restaurant.dto.ItemAttributeResponse;
import com.ps.restaurant.dto.filters.FilterItemAttributeRequest;
import com.ps.restaurant.interfaces.ItemAttributeService;
import com.ps.restaurant.model.ItemAttribute;
import com.ps.restaurant.repository.ItemAttributesRepository;
import com.ps.restaurant.repository.specification.ItemAttributeSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemAttributeServiceImpl implements ItemAttributeService {

    private final ItemAttributesRepository repository;

    @Override
    public Optional<ItemAttribute> getItemAttributeById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ItemAttributeResponse> filterItemAttributes(FilterItemAttributeRequest filter) {
        return repository.findAll(ItemAttributeSpecifications.withFilters(filter)).stream()
                .map(ItemAttributeResponse::from)
                .toList();
    }

    @Override
    public long countItemAttributes(FilterItemAttributeRequest filter) {
        return repository.count(ItemAttributeSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ItemAttributeResponse createItemAttribute(ItemAttributeRequest request) {
        if (repository.existsByAttributeKey(request.attributeKey())) {
            throw new ConflictException("ItemAttribute with this attributeKey already exists");
        }

        ItemAttribute entity = new ItemAttribute();
        entity.setAttributeKey(request.attributeKey());
        entity.setDisplayName(request.displayName());
        return ItemAttributeResponse.from(repository.save(entity));
    }
}

