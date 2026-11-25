package com.ps.restaurant.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ps.restaurant.config.Permissions;
import com.ps.restaurant.dto.ErrorResponseException;

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
        requireInactiveAccess(filter);
        var criteria = ItemSpecifications.withFilters(filter);
        var items = filter.size() == null && filter.page() == null ? itemsRepository.findAll(criteria)
                : itemsRepository.findAll(criteria, PageRequest.of(filter.page() == null ? 0 : filter.page(),
                        filter.size() == null ? 50 : filter.size(), Sort.by("id"))).getContent();
        return items.stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Override
    public long countItems(FilterItemRequest filter) {
        requireInactiveAccess(filter);
        return itemsRepository.count(ItemSpecifications.withFilters(filter));
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        if (itemsRepository.existsBySku(request.sku())) {
            throw new ConflictException("Item with this sku already exists");
        }
        Item item = new Item();
        apply(item, request);
        return ItemResponse.from(itemsRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = ServiceSupport.requireById(itemsRepository, id, "Item");
        if (!item.getSku().equals(request.sku()) && itemsRepository.existsBySku(request.sku())) {
            throw new ConflictException("Item with this sku already exists");
        }
        apply(item, request);
        return ItemResponse.from(item);
    }

    private void requireInactiveAccess(FilterItemRequest filter) {
        if (Boolean.TRUE.equals(filter.includeInactive())
                && !Permissions.has(SecurityContextHolder.getContext().getAuthentication(), Permissions.ITEM_MANAGE)) {
            throw new AccessDeniedException("Viewing inactive menu items requires items:manage");
        }
    }

    private void apply(Item item, ItemRequest request) {
        if (request.img() != null && !request.img().isBlank()
                && !request.img().matches("(?i)(https?://[^\\s]+|/?[a-z0-9_][a-z0-9_./%?&=+#-]*)")) {
            throw new ErrorResponseException(422, "INVALID_ITEM_IMAGE", "Use a valid relative or HTTP(S) image URL");
        }
        item.setSku(request.sku());
        item.setDisplayName(request.displayName());
        item.setBarcodes(request.barcodes());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(ServiceSupport.requireById(categoryRepository, request.categoryId(), "Category"));
        item.setAvailable(request.available());
        if (request.active() != null) item.setActive(request.active());
        item.setImg(request.img());
        item.setAvailableAttributes(request.attributeIds() == null ? List.of()
                : request.attributeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(attributesRepository, id, "Item attribute"))
                        .toList());
        item.setSizes(request.sizeIds() == null ? List.of()
                : request.sizeIds().stream().distinct()
                        .map(id -> ServiceSupport.requireById(sizeRepository, id, "Item size"))
                        .toList());
    }

}
