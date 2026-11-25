package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ItemSizeRequest;
import com.ps.restaurant.dto.ItemSizeResponse;
import com.ps.restaurant.dto.filters.FilterItemSizeRequest;
import com.ps.restaurant.interfaces.ItemSizeService;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.repository.ItemSizeRepository;
import com.ps.restaurant.repository.specification.ItemSizeSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemSizeServiceImpl implements ItemSizeService {

    private final ItemSizeRepository repository;

    @Override
    public Optional<ItemSize> getItemSizeById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ItemSizeResponse> filterItemSizes(FilterItemSizeRequest filter) {
        return repository.findAll(ItemSizeSpecifications.withFilters(filter)).stream()
                .map(ItemSizeResponse::from)
                .toList();
    }

    @Override
    public long countItemSizes(FilterItemSizeRequest filter) {
        return repository.count(ItemSizeSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ItemSizeResponse createItemSize(ItemSizeRequest request) {
        if (repository.existsBySizeKey(request.sizeKey())) {
            throw new ConflictException("ItemSize with this sizeKey already exists");
        }

        ItemSize entity = new ItemSize();
        entity.setSizeKey(request.sizeKey());
        entity.setDisplayName(request.displayName());
        return ItemSizeResponse.from(repository.save(entity));
    }
}

