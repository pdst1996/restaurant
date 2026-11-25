package com.ps.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.ItemAttribute;

public interface ItemAttributesRepository extends JpaRepository<ItemAttribute, Long>, JpaSpecificationExecutor<ItemAttribute> {

    public List<ItemAttribute> findByAttributeKey(String attributeKey);

    public boolean existsByAttributeKey(String attributeKey);

}
