package com.ps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Floor;
import java.util.Optional;

public interface FloorRepository extends JpaRepository<Floor, Long>, JpaSpecificationExecutor<Floor>{
    boolean existsByName(String name);
    Optional<Floor> findFirstByNameOrderByIdAsc(String name);
}
