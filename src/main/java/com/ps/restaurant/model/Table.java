package com.ps.restaurant.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private Integer tableNumber;

    private Integer capacity;

    @OneToMany(mappedBy = "table")
    private List<Order> orders = new ArrayList<>();
}
