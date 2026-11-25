package com.ps.restaurant.interfaces;

import java.util.List;
import com.ps.restaurant.dto.ElementPlanRequest;
import com.ps.restaurant.dto.ElementPlanResponse;
import com.ps.restaurant.dto.filters.FilterElementPlanRequest;

public interface ElementPlanService {
    List<ElementPlanResponse> filterElements(FilterElementPlanRequest filter);
    long countElements(FilterElementPlanRequest filter);
    ElementPlanResponse createElement(ElementPlanRequest request);
}
