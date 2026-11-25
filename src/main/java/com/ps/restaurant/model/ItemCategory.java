package com.ps.restaurant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Groups menu products under a unique name. Lombok generates field accessors and
 * a no-argument constructor; products start in an empty mutable list.
 */
@Entity 
@Table (name = "categories")
@Getter 
@Setter 
@NoArgsConstructor 
public class ItemCategory {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    String categoryKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column (nullable = false)
    private Map<String, String> displayName;

    @OneToMany (mappedBy = "category")
    private List<Item> products = new ArrayList<>();
}
