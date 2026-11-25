package com.ps.restaurant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@jakarta.persistence.Table (name = "element")
@Getter 
@Setter 
@NoArgsConstructor 
public class Element {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    private Floor floor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    private ElementType elementType;

    private Integer x, y, width, height, rotation = 0;
    
    private String label;
}
