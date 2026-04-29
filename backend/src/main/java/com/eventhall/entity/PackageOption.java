package com.eventhall.entity;

import com.eventhall.dto.PackageOptionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a selectable option within an event package
 * (e.g. catering tier, decoration style, DJ service).
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
     * Broad grouping used to organise options in the builder UI.
     * Stored as a string (not ordinal) so renaming values is safe.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PackageOptionCategory category;

    /**
     * The default price for this option in ILS.
     * Customer-specific overrides in CustomerOptionPriceOverride
     * supersede this value for the relevant customer.
     */
    @Column(name = "global_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal globalPrice;

    /** URL of the option's display image (used in the visual builder). */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * Visual behavior hint for the builder UI.
     * Values: REPLACE_IMAGE | OVERLAY_IMAGE | NO_VISUAL
     */
    @Column(name = "visual_behavior", length = 30)
    private String visualBehavior;

    /** CSS top position when used as an overlay (e.g. "10%", "40px"). */
    @Column(name = "overlay_top", length = 30)
    private String overlayTop;

    /** CSS left position when used as an overlay. */
    @Column(name = "overlay_left", length = 30)
    private String overlayLeft;

    /** CSS width when used as an overlay (e.g. "80%", "200px"). */
    @Column(name = "overlay_width", length = 30)
    private String overlayWidth;

    /** CSS z-index when used as an overlay. */
    @Column(name = "overlay_z_index")
    private Integer overlayZIndex;

    /**
     * Which table context this option applies to.
     * Values: REGULAR | KNIGHT | BOTH (null = BOTH for backward compatibility).
     * Only relevant for TABLE_FRAME, TABLE_FLOWER, TABLE_CANDLE categories.
     */
    @Column(name = "table_context", length = 10)
    private String tableContext;

    /**
     * Flower size for TABLE_FLOWER options.
     * Values: LARGE | SMALL (null = not applicable or LARGE by default).
     * LARGE flowers allow an optional secondary SMALL flower.
     * SMALL flowers cannot have a secondary flower.
     */
    @Column(name = "flower_size", length = 10)
    private String flowerSize;

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
