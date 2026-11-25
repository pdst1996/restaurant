package com.ps.restaurant.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column (nullable = false)
    private Integer quantity;

    @JoinColumn(name="item_size", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ItemSize size;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_item_attributes",
        joinColumns = @JoinColumn(name = "order_item_id"),
        inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private List<ItemAttribute> attributes = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private String notes;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    private OrderItemStatus status = OrderItemStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
