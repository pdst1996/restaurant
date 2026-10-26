package com.ps.restaurant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Stores a named role and activation flag. Lombok generates accessors, a required-arguments
 * constructor (no arguments here), and field-based equals, hashCode, and toString methods.
 */
@Entity 
@Table (name = "roles")
@Data 
public class Role {
     
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column (nullable = false)
    private boolean active;
}
