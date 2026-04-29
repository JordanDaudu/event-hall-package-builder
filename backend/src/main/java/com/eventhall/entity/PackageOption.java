package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a selectable option within an event package (e.g. catering tier,
 * decoration style, DJ service).
 *
 * Phase 4 scaffold: contains only the fields required so that
 * CustomerOptionPriceOverride can hold a real FK constraint. Full business
 * logic (category, validation, admin CRUD) will be added in Phase 6.
 *
 * FK contract:
 *   customer_option_price_overrides.option_id → package_options.id ON DELETE CASCADE
 */
@Entity
@Table(name = "package_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Hebrew display name shown in the package builder UI. */
    @Column(name = "name_he", nullable = false, length = 120)
    private String nameHe;

    /** English / internal name (optional, used for admin tooling). */
    @Column(name = "name_en", length = 120)
    private String nameEn;

    /**
     * The default price for this option in ILS.
     * Customer-specific overrides stored in CustomerOptionPriceOverride
     * supersede this value for the relevant customer.
     */
    @Column(name = "global_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal globalPrice;

    /** Controls whether the option appears in the customer-facing builder. */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Determines display order in the builder UI (ascending). */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

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
