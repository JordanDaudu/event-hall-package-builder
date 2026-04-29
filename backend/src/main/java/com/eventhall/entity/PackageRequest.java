package com.eventhall.entity;

import com.eventhall.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An event package request submitted by a customer.
 *
 * Price fields are snapshotted at submission time so that
 * later price changes never affect already-submitted or approved requests.
 *
 * Status transitions: PENDING → APPROVED or PENDING → REJECTED only.
 */
@Entity
@Table(name = "package_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserAccount customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "event_customer_identity_number", nullable = false, length = 50)
    private String eventCustomerIdentityNumber;

    @Column(name = "event_contact_name", nullable = false, length = 200)
    private String eventContactName;

    @Column(name = "event_contact_phone_number", nullable = false, length = 50)
    private String eventContactPhoneNumber;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    /** Snapshot of venue.nameHe at submission time. */
    @Column(name = "venue_name_snapshot", nullable = false, length = 200)
    private String venueNameSnapshot;

    /** Snapshot of customer.basePackagePrice at submission time. */
    @Column(name = "base_package_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePackagePriceSnapshot;

    /** basePackagePriceSnapshot + sum of all item finalPrices. */
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /** Admin notes set when approving or rejecting the request. */
    @Column(name = "summary_notes", columnDefinition = "TEXT")
    private String summaryNotes;

    /** Optional knight table count for the event. */
    @Column(name = "knight_table_count")
    private Integer knightTableCount;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    /** Timestamp when status changed to APPROVED or REJECTED. */
    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "packageRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PackageRequestItem> items = new ArrayList<>();

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
