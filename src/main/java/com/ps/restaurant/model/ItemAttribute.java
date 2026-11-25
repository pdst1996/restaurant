package com.ps.restaurant.model;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@jakarta.persistence.Table (name = "item_attributes")
@Getter 
@Setter 
@NoArgsConstructor 
public class ItemAttribute {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column (nullable = false, unique = true)
    String attributeKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column (nullable = false)
    Map<String, String> displayName;
}
