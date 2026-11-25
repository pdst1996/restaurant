package com.ps.restaurant.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "items")
@Getter 
@Setter 
@NoArgsConstructor 
public class Item {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column (nullable = false, unique = true)
    String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column (nullable = false)
    Map<String, String> displayName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column (nullable = true)
    Map<String, Object> barcodes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "item_sizes",
        joinColumns = @JoinColumn(name = "item_id"),
        inverseJoinColumns = @JoinColumn(name = "size_id")
    )
    List<ItemSize> sizes = new ArrayList<>();

    @Column (nullable = true)
    String description;

    @Column (nullable = false, precision = 12, scale = 2)
    BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategory category;

    @OneToMany(mappedBy = "item")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(nullable = true, length = 2048)
    private String img;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "item_attribute_links",
        joinColumns = @JoinColumn(name = "item_id"),
        inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private List<ItemAttribute> availableAttributes = new ArrayList<>();

}
