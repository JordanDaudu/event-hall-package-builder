package com.eventhall.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/*
 * Entity for the event_type table.
 *
 * EventType represents the base type of event the customer selects.
 * Example rows:
 * - Wedding, base price 120
 * - Birthday, base price 80
 * - Corporate Event, base price 100
 */
@Entity
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /*
     * BigDecimal is used for prices because it is safer for money values
     * than floating point types like double or float.
     */
    private BigDecimal basePrice;

    /*
     * Required by JPA.
     * JPA creates objects using this constructor when reading from the database.
     */
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