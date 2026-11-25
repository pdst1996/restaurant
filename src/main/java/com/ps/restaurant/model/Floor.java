package com.ps.restaurant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@jakarta.persistence.Table (name = "floor")
@Getter 
@Setter 
@NoArgsConstructor 
public class Floor {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private Integer width = 1200;

    @Column (nullable = false)
    private Integer height = 800;

    @Column (nullable = false)
    private Integer sort_order = 0;

}
