package com.ps.restaurant.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Element;

public interface ElementPlanRepository extends JpaRepository<Element, Long>, JpaSpecificationExecutor<Element>{
    boolean existsByFloorIdAndElementTypeTypeAndXAndY(Long floorId, String type, Integer x, Integer y);
}
