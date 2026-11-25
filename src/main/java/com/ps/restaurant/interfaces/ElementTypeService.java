package com.ps.restaurant.interfaces;

import java.util.List;
import com.ps.restaurant.dto.ElementTypeRequest;
import com.ps.restaurant.dto.ElementTypeResponse;
import com.ps.restaurant.dto.filters.FilterElementTypeRequest;

public interface ElementTypeService {
    List<ElementTypeResponse> filterElementTypes(FilterElementTypeRequest filter);
    long countElementTypes(FilterElementTypeRequest filter);
    ElementTypeResponse createElementType(ElementTypeRequest request);
}
