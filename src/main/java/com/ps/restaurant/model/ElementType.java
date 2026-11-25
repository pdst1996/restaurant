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
@jakarta.persistence.Table (name = "element_type")
@Getter 
@Setter 
@NoArgsConstructor 
public class ElementType {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON) 
    private Map<String, String> label;

    private Integer width, height;

    private boolean round;

    private String fill, stroke;
    
}
