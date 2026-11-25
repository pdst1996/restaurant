package com.ps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
    @org.springframework.data.jpa.repository.Query("select i.order.id from OrderItem i where i.id = :id")
    java.util.Optional<Long> findParentId(@org.springframework.data.repository.query.Param("id") Long id);
}

