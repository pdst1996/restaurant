package com.ps.restaurant.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
 * Stores a product selection, quantity, size, attributes, and unit price for an
 * order. Lombok generates accessors and a no-argument constructor; no pricing
 * calculation is performed by the generated setters.
 */
@Entity 
@Table (name = "order_items")
@Getter 
@Setter 
@NoArgsConstructor 
public class OrderItem {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column (nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private ItemSize size;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> attributes = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
