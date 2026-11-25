package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.ItemRequest;
import com.ps.restaurant.dto.ItemResponse;
import com.ps.restaurant.dto.filters.FilterItemRequest;
import com.ps.restaurant.model.Item;

public interface ItemService {

    /**
     * Returns the item with the given identifier, or empty if it does not exist.
     */
    public Optional<Item> getItemById(Long id);

    /**
     * Get the items filtered
     *
     * @param filter the FilterItemRequest filter
     * @return a list of items filtered
     */
    public List<ItemResponse> filterItems(FilterItemRequest filter);

    long countItems(FilterItemRequest filter);

    /**
     * Save the item
     *
     * @param itemRequest
     * @return the item created
     */
    public ItemResponse createItem(ItemRequest itemRequest);

}
