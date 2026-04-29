package com.eventhall.dto;

import com.eventhall.entity.PackageRequest;
import com.eventhall.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full detail of a PackageRequest including all snapshotted fields and line items.
 */
public record PackageRequestDetailResponse(
        Long id,
        Long customerId,
        String customerEmail,
        String customerFullName,
        RequestStatus status,
        String eventCustomerIdentityNumber,
        String eventContactName,
        String eventContactPhoneNumber,
        LocalDate eventDate,
        String venueNameSnapshot,
        BigDecimal basePackagePriceSnapshot,
        BigDecimal totalPrice,
        String summaryNotes,
        Integer knightTableCount,
        Instant submittedAt,
        Instant decidedAt,
        Instant createdAt,
        List<PackageRequestItemResponse> items
) {
    public static PackageRequestDetailResponse from(PackageRequest r, List<PackageRequestItemResponse> items) {
        return new PackageRequestDetailResponse(
                r.getId(),
                r.getCustomer().getId(),
                r.getCustomer().getEmail(),
                r.getCustomer().getFullName(),
                r.getStatus(),
                r.getEventCustomerIdentityNumber(),
                r.getEventContactName(),
                r.getEventContactPhoneNumber(),
                r.getEventDate(),
                r.getVenueNameSnapshot(),
                r.getBasePackagePriceSnapshot(),
                r.getTotalPrice(),
                r.getSummaryNotes(),
                r.getKnightTableCount(),
                r.getSubmittedAt(),
                r.getDecidedAt(),
                r.getCreatedAt(),
                items
        );
    }
}
