package com.ps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.ElementType;
import java.util.Optional;

public interface ElementTypeRepository extends JpaRepository<ElementType, Long>, JpaSpecificationExecutor<ElementType> {
    boolean existsByType(String type);
    Optional<ElementType> findByType(String type);
}
