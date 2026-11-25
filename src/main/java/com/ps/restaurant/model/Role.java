package com.ps.restaurant.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean permissionsInitialized;

    @ElementCollection
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission"}))
    @Column(name = "permission", nullable = false, length = 64)
    private Set<String> permissions = new HashSet<>();
}
