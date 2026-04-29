package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single selected option within a PackageRequest.
 *
 * All price values are snapshotted at submission time:
 * - globalPriceSnapshot: the option's global price at submission
 * - customerOverridePriceSnapshot: the customer's price override (null if none existed)
 * - finalPrice: the price actually used (override if present, else global)
 *
 * The FK to package_options is nullable-in-practice (via FetchType.LAZY)
 * but the snapshot fields ensure historical data is always complete even
 * if the source option is later deleted.
 */
@Entity
@Table(name = "package_request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_request_id", nullable = false)
    private PackageRequest packageRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_option_id", nullable = false)
    private PackageOption packageOption;

    /** Snapshot of packageOption.nameHe at submission time. */
    @Column(name = "option_name_snapshot", nullable = false, length = 120)
    private String optionNameSnapshot;

    /** Snapshot of packageOption.globalPrice at submission time. */
    @Column(name = "global_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal globalPriceSnapshot;

    /**
     * The customer-specific override price at submission time.
     * Null if no override existed for this customer+option combination.
     */
    @Column(name = "customer_override_price_snapshot", precision = 12, scale = 2)
    private BigDecimal customerOverridePriceSnapshot;

    /**
     * The price actually used: customerOverridePriceSnapshot if not null,
     * otherwise globalPriceSnapshot.
     */
    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
