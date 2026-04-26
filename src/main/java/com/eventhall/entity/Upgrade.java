package com.eventhall.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Upgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private boolean active;

    protected Upgrade() {
    }

    public Upgrade(String name, String description, String category, BigDecimal price, boolean active) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }
}