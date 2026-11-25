package com.ps.restaurant.model;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a restaurant table's number, capacity, and related orders. Lombok generates
 * accessors and a no-argument constructor; callers maintain both sides of the order relationship.
 */
@Entity 
@jakarta.persistence.Table (name = "restaurant_tables")
@Getter 
@Setter 
@NoArgsConstructor 
public class Table {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tableName;

    private Integer capacity;

    @OneToMany(mappedBy = "table")
    private List<Order> orders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TableShape shape = TableShape.RECTANGULAR;

    @Column (nullable = true)
    @Enumerated(EnumType.STRING)
    private TableStatus tableStatus = TableStatus.FREE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column (nullable = true)
    private Integer x;

    @Column (nullable = true)
    private Integer y;

    @Column (nullable = true)
    private Integer width = 100;

    @Column (nullable = true)
    private Integer height = 80;

    @Column (nullable = true)
    private Integer rotation = 0;
}
