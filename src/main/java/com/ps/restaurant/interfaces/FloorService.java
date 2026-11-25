package com.ps.restaurant.interfaces;

import java.util.List;

import com.ps.restaurant.dto.FloorRequest;
import com.ps.restaurant.dto.FloorResponse;
import com.ps.restaurant.dto.LayoutRequest;
import com.ps.restaurant.dto.LayoutResponse;
import com.ps.restaurant.dto.filters.FilterFloorRequest;

public interface FloorService {

    public List<FloorResponse> filterFloors(FilterFloorRequest filter);

    public FloorResponse createFloor(FloorRequest request);

    long countFloors(FilterFloorRequest filter);

    LayoutResponse saveLayout(LayoutRequest request);

}
