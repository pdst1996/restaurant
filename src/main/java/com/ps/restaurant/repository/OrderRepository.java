package com.ps.restaurant.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ps.restaurant.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findById(Long id);

    Optional<Order> findByOrderNumber(UUID orderNumber);

    List<Order> findAll(Specification<Order> orderSpecifications);

}
