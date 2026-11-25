package com.ps.restaurant.model;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@jakarta.persistence.Table(name = "order_status_history")
public class OrderStatusChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long orderId;
    private Long actorId;
    @Enumerated(EnumType.STRING) private OrderStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OrderStatus toStatus;
    @Column(length = 500) private String reason;
    @Column(nullable = false) private Instant createdAt;

    protected OrderStatusChange() {}

    public OrderStatusChange(Long orderId, Long actorId, OrderStatus fromStatus, OrderStatus toStatus, String reason) {
        this.orderId = orderId;
        this.actorId = actorId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getActorId() { return actorId; }
    public OrderStatus getFromStatus() { return fromStatus; }
    public OrderStatus getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
