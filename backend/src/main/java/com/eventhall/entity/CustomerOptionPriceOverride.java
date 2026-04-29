package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Stores a customer-specific price override for a package option.
 *
 * When no override exists for a (customer, option) pair, the global price
 * defined on the PackageOption itself is used instead.
 *
 * optionId is a forward reference to the package_options table which will
 * be created in Phase 6. Until then it is stored as a plain Long column
 * (no FK constraint) so that the override infrastructure can be put in
 * place independently.
 */
@Entity
@Table(
        name = "customer_option_price_overrides",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_option_override",
                        columnNames = {"customer_id", "option_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOptionPriceOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The customer this override belongs to. Must be a CUSTOMER-role UserAccount.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cop_override_customer"))
    private UserAccount customer;

    /**
     * Forward reference to the package_options table (Phase 6).
     * Stored as a plain Long (no FK constraint) until that table exists.
     */
    @Column(name = "option_id", nullable = false)
    private Long optionId;

    /**
     * The custom price that overrides the global package option price for this customer.
     */
    @Column(name = "custom_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal customPrice;

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
