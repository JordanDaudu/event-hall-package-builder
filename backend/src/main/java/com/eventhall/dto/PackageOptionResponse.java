package com.eventhall.dto;

import com.eventhall.entity.PackageOption;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for a package option.
 * Returned by both the public listing and the admin endpoints.
 */
public record PackageOptionResponse(
        Long id,
        String nameHe,
        String nameEn,
        PackageOptionCategory category,
        BigDecimal globalPrice,
        boolean active,
        int sortOrder,
        String imageUrl,
        String visualBehavior,
        String overlayTop,
        String overlayLeft,
        String overlayWidth,
        Integer overlayZIndex,
        /** REGULAR | KNIGHT | BOTH (null = BOTH). Only relevant for table categories. */
        String tableContext,
        /** LARGE | SMALL (null = not applicable). Only relevant for TABLE_FLOWER. */
        String flowerSize,
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
                o.getImageUrl(),
                o.getVisualBehavior(),
                o.getOverlayTop(),
                o.getOverlayLeft(),
                o.getOverlayWidth(),
                o.getOverlayZIndex(),
                o.getTableContext(),
                o.getFlowerSize(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
