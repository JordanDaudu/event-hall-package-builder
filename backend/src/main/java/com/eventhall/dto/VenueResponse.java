package com.eventhall.dto;

import com.eventhall.entity.Venue;

import java.time.Instant;

/**
 * Returned for both public and admin venue endpoints.
 * The admin view includes createdAt/updatedAt; the same record is safe to
 * return to customers because no sensitive data is held on venues.
 */
public record VenueResponse(
        Long id,
        String nameHe,
        String nameEn,
        String descriptionHe,
        String imageUrl,
        boolean active,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
    public static VenueResponse from(Venue v) {
        return new VenueResponse(
                v.getId(),
                v.getNameHe(),
                v.getNameEn(),
                v.getDescriptionHe(),
                v.getImageUrl(),
                v.isActive(),
                v.getSortOrder(),
                v.getCreatedAt(),
                v.getUpdatedAt()
        );
    }
}
