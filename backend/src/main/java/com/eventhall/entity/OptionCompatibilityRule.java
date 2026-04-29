package com.eventhall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Defines which CHUPPAH_UPGRADE options are compatible with a given CHUPPAH option.
 *
 * parentOption: the main chuppah (category = CHUPPAH)
 * childOption:  the allowed upgrade (category = CHUPPAH_UPGRADE)
 */
@Entity
@Table(
    name = "option_compatibility_rules",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"parent_option_id", "child_option_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionCompatibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_option_id", nullable = false)
    private PackageOption parentOption;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "child_option_id", nullable = false)
    private PackageOption childOption;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
