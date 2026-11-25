package com.ps.restaurant.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ps.restaurant.dto.ApiResponse;
import com.ps.restaurant.dto.FloorRequest;
import com.ps.restaurant.dto.FloorResponse;
import com.ps.restaurant.dto.LayoutRequest;
import com.ps.restaurant.dto.LayoutResponse;
import com.ps.restaurant.dto.filters.FilterFloorRequest;
import com.ps.restaurant.interfaces.FloorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/floor/floors")
@RequiredArgsConstructor
public class FloorController {
    private final FloorService floorService;

    @GetMapping
    public ResponseEntity<List<FloorResponse>> getFloors(@Valid @ModelAttribute FilterFloorRequest filter) {
        return ResponseEntity.ok(floorService.filterFloors(filter));
    }

    @PostMapping
    public ResponseEntity<FloorResponse> createFloor(@Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(floorService.createFloor(request));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countFloors(@Valid @ModelAttribute FilterFloorRequest filter) {
        return ResponseEntity.ok(floorService.countFloors(filter));
    }

    @PutMapping("/floor-plan")
    public ApiResponse<LayoutResponse> saveLayout(@Valid @RequestBody LayoutRequest request) {
        return ApiResponse.ok("Layout saved successfully", floorService.saveLayout(request));
    }
}
