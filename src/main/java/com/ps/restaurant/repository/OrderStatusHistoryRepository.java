package com.ps.restaurant.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ps.restaurant.model.OrderStatusChange;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusChange, Long> {
    List<OrderStatusChange> findByOrderIdOrderByIdAsc(Long orderId);
}
