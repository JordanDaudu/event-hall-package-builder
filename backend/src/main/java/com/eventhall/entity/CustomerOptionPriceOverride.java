package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Stores a customer-specific price override for a package option.
 *
 * When no override exists for a (customer, option) pair, the global price
 * defined on the PackageOption itself is used instead.
 *
 * The option_id column carries a real FK constraint referencing
 * package_options(id) with ON DELETE CASCADE, so that removing a package
 * option automatically removes all customer overrides for that option.
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
     * The package option this override applies to.
     *
     * @JoinColumn(name = "option_id") reuses the existing column so
     * Hibernate's ddl-auto=update only needs to add the FK constraint
     * rather than create a new column.
     *
     * @OnDelete(CASCADE) delegates cascade to the database so that
     * deleting a PackageOption automatically removes all its overrides
     * without requiring JPA to load them first.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cop_override_option"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PackageOption packageOption;

    /**
     * The custom price that overrides the global package option price for this customer.
     */
    @Column(name = "custom_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal customPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Convenience accessor — avoids loading the full entity in response mappers. */
    public Long getPackageOptionId() {
        return packageOption != null ? packageOption.getId() : null;
    }

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
