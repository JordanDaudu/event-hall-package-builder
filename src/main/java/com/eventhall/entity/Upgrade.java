package com.eventhall.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/*
 * Entity for upgrade options.
 *
 * An Upgrade is an optional add-on the customer can choose while building a package.
 * Examples:
 * - Flowers
 * - DJ
 * - Lighting
 * - Live Plants
 * - Stage Design
 */
@Entity
public class Upgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String category;
    private BigDecimal price;

    /*
     * active is used for soft delete.
     *
     * If active = true, customers can see and select the upgrade.
     * If active = false, the upgrade is hidden from the public list,
     * but the database row still exists for historical quote data.
     */
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

    /*
     * Domain method for updating upgrade details.
     *
     * This keeps all update logic inside the entity instead of letting the service
     * directly set every field one by one.
     */
    public void updateDetails(
            String name,
            String description,
            String category,
            BigDecimal price,
            boolean active
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.active = active;
    }

    /*
     * Soft delete method.
     *
     * Instead of deleting the row from the database, we mark it inactive.
     * This is safer because older quotes may still reference this upgrade.
     */
    public void deactivate() {
        this.active = false;
    }
}