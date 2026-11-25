package com.ps.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Item;

public interface ItemsRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    public boolean existsBySku(String sku);

    public Optional<Item> findById(Long id);
    

}
