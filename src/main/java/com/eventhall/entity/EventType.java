package com.eventhall.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal basePrice;

    // Required by JPA.
    // JPA creates objects using this constructor when reading from the database.
    protected EventType() {
    }

    public EventType(String name, BigDecimal basePrice) {
        this.name = name;
        this.basePrice = basePrice;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}