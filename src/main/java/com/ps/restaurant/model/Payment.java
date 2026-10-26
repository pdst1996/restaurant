package com.ps.restaurant.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Records an amount and payment method for an order. Lombok generates field
 * accessors and a no-argument constructor; assigning a payment does not update the order status.
 */
@Entity 
@Table (name = "payments")
@Getter 
@Setter 
@NoArgsConstructor 
public class Payment {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "order_id", nullable = false)
    private Order order;

    @Column (nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
