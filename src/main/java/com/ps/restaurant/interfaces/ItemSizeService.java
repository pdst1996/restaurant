package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.ItemSizeRequest;
import com.ps.restaurant.dto.ItemSizeResponse;
import com.ps.restaurant.dto.filters.FilterItemSizeRequest;
import com.ps.restaurant.model.ItemSize;

public interface ItemSizeService {

    Optional<ItemSize> getItemSizeById(Long id);

    List<ItemSizeResponse> filterItemSizes(FilterItemSizeRequest filter);

    long countItemSizes(FilterItemSizeRequest filter);

    ItemSizeResponse createItemSize(ItemSizeRequest request);
}

