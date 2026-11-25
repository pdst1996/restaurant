package com.ps.restaurant.interfaces;

import java.util.List;
import java.util.Optional;

import com.ps.restaurant.dto.ItemAttributeRequest;
import com.ps.restaurant.dto.ItemAttributeResponse;
import com.ps.restaurant.dto.filters.FilterItemAttributeRequest;
import com.ps.restaurant.model.ItemAttribute;

public interface ItemAttributeService {

    Optional<ItemAttribute> getItemAttributeById(Long id);

    List<ItemAttributeResponse> filterItemAttributes(FilterItemAttributeRequest filter);

    long countItemAttributes(FilterItemAttributeRequest filter);

    ItemAttributeResponse createItemAttribute(ItemAttributeRequest request);
}

