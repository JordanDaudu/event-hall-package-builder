package com.eventhall.dto;

import com.eventhall.entity.PackageRequest;
import com.eventhall.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Lightweight summary of a PackageRequest, used in list endpoints.
 */
public record PackageRequestSummaryResponse(
        Long id,
        RequestStatus status,
        BigDecimal totalPrice,
        LocalDate eventDate,
        String venueNameSnapshot,
        String eventContactName,
        Instant submittedAt,
        Instant createdAt
) {
    public static PackageRequestSummaryResponse from(PackageRequest r) {
        return new PackageRequestSummaryResponse(
                r.getId(),
                r.getStatus(),
                r.getTotalPrice(),
                r.getEventDate(),
                r.getVenueNameSnapshot(),
                r.getEventContactName(),
                r.getSubmittedAt(),
                r.getCreatedAt()
        );
    }
}
