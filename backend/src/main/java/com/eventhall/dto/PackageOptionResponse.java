package com.eventhall.dto;

import com.eventhall.entity.PackageOption;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for a package option.
 * Returned by both the public listing and the admin endpoints.
 * The same record is safe for customers (no sensitive pricing data beyond
 * the global price, which is intentionally visible in the builder UI).
 */
public record PackageOptionResponse(
        Long id,
        String nameHe,
        String nameEn,
        PackageOptionCategory category,
        BigDecimal globalPrice,
        boolean active,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static PackageOptionResponse from(PackageOption o) {
        return new PackageOptionResponse(
                o.getId(),
                o.getNameHe(),
                o.getNameEn(),
                o.getCategory(),
                o.getGlobalPrice(),
                o.isActive(),
                o.getSortOrder(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
