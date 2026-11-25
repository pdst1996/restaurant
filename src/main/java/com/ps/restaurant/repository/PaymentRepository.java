package com.ps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ps.restaurant.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    java.util.Optional<Payment> findByOrderIdAndIdempotencyKey(Long orderId, String key);

    @org.springframework.data.jpa.repository.Query("select p.order.id from Payment p where p.id = :id")
    java.util.Optional<Long> findParentId(@org.springframework.data.repository.query.Param("id") Long id);
}

