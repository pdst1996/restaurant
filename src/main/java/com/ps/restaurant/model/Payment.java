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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Records an amount and payment method for an order. Lombok generates field
 * accessors and a no-argument constructor; assigning a payment does not update the order status.
 */
@Entity 
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(
        name = "payments_order_idempotency_uk", columnNames = {"order_id", "idempotency_key"}))
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'COMPLETED'")
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "numeric(12,2) default 0")
    private BigDecimal tip = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2) private BigDecimal cashReceived;
    @Column(name = "cash_change", precision = 12, scale = 2) private BigDecimal change;
    @Column(length = 100) private String externalReference;
    @Column(length = 100) private String refundReference;
    private Long collectedBy;
    private Long refundedBy;
    private Instant refundedAt;
    @Enumerated(EnumType.STRING) private PaymentStatus reversedFrom;
    @Column(length = 500) private String refundReason;
    @Column(length = 80, updatable = false) private String idempotencyKey;
    @Column(length = 64, updatable = false) private String requestFingerprint;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
