package com.eventhall.entity;

import com.eventhall.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * UserAccount represents anyone who can log into the system.
 *
 * - For CUSTOMER role users, {@code basePackagePrice} and {@code customerIdentityNumber}
 *   carry per-customer business data used by the package builder.
 * - For ADMIN role users, those fields are typically null.
 *
 * Note: the database {@code id} is internal; the business-facing customer identifier
 * is {@code customerIdentityNumber} (e.g. an Israeli ID) and must NEVER be confused
 * with the internal id or used as a password.
 */
@Entity
@Table(
        name = "user_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_account_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_account_identity", columnNames = "customer_identity_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /**
     * Business identifier for customers (e.g. Israeli ID).
     * Nullable for admin users.
     */
    @Column(name = "customer_identity_number", length = 50)
    private String customerIdentityNumber;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    /**
     * BCrypt hash of the user's password. Never returned by any API.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Per-customer base package price (in ILS). Only meaningful for CUSTOMER users.
     */
    @Column(name = "base_package_price", precision = 12, scale = 2)
    private BigDecimal basePackagePrice;

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
