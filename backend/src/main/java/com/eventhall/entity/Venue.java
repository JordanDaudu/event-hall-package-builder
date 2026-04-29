package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents a physical event hall venue.
 *
 * Business rules:
 * - Only active venues are returned to customers.
 * - Soft-delete is used (active = false) to preserve historical references
 *   once package requests start referencing venues.
 * - sortOrder controls display order in the customer-facing venue picker.
 * - priceModifier is kept nullable for a future pricing feature; not used yet.
 */
@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_he", nullable = false, length = 200)
    private String nameHe;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "description_he", columnDefinition = "TEXT")
    private String descriptionHe;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
