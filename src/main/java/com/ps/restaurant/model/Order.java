package com.ps.restaurant.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores an order with its waiter, optional table, items, and payments. Lombok
 * generates field accessors and a no-argument constructor. New orders start open
 * with zero total; callers must maintain totals and both sides of relationships.
 */
@Entity 
@Table (name = "orders")
@Getter 
@Setter 
@NoArgsConstructor 
public class Order {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 255)
    private String orderNumber;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "waiter_id", nullable = false)
    private User waiter;

    @Column (nullable = true)
    private String customer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_allergies",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private List<Allergy> allergies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id",  nullable = true)
    private com.ps.restaurant.model.Table table;

    @OneToMany (
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(
        mappedBy = "order",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<Payment> payments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(nullable = true)
    private boolean togo = false;

    @Column(nullable = true)
    private String notes;

    @Column(nullable = true)
    private String otherAllergies;

    @Column(nullable = true)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(precision = 5, scale = 4)
    private BigDecimal taxRate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
