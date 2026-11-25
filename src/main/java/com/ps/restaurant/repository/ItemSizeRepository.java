package com.ps.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.ItemSize;

public interface ItemSizeRepository extends JpaRepository<ItemSize, Long>, JpaSpecificationExecutor<ItemSize> {
    public boolean existsBySizeKey(String sizeKey);

    public Optional<ItemSize> getBySizeKey(String sizeKey);
}
