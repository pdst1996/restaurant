package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.ErrorResponseException.ConflictException;
import com.ps.restaurant.dto.ItemRequest;
import com.ps.restaurant.dto.ItemResponse;
import com.ps.restaurant.dto.filters.FilterItemRequest;
import com.ps.restaurant.interfaces.ItemService;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.repository.ItemsRepository;
import com.ps.restaurant.repository.CategoryRepository;
import com.ps.restaurant.repository.ItemAttributesRepository;
import com.ps.restaurant.repository.ItemSizeRepository;
import com.ps.restaurant.repository.specification.ItemSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemsRepository itemsRepository;
    private final CategoryRepository categoryRepository;
    private final ItemAttributesRepository attributesRepository;
    private final ItemSizeRepository sizeRepository;

    @Override
    public Optional<Item> getItemById(Long id) {
        return itemsRepository.findById(id);
    }

    @Override
    public List<ItemResponse> filterItems(FilterItemRequest filter) {
        return itemsRepository.findAll(ItemSpecifications.withFilters(filter)).stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Override
    public long countItems(FilterItemRequest filter) {
        return itemsRepository.count(ItemSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        if (itemsRepository.existsBySku(request.sku())) {
            throw new ConflictException("Item with this sku already exists");
        }
        Item item = new Item();
        item.setSku(request.sku());
        item.setDisplayName(request.displayName());
        item.setBarcodes(request.barcodes());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(ServiceSupport.requireById(categoryRepository, request.categoryId(), "Category"));
        item.setAvailable(request.available());
        item.setImg(request.img());
        item.setAvailableAttributes(request.attributeIds() == null ? List.of()
                : request.attributeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(attributesRepository, id, "Item attribute"))
                        .toList());
        item.setSizes(request.sizeIds() == null ? List.of()
                : request.sizeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(sizeRepository, id, "Item size"))
                        .toList());
        return ItemResponse.from(itemsRepository.save(item));
    }

}
