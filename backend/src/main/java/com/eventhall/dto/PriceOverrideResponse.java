package com.eventhall.dto;

import com.eventhall.entity.CustomerOptionPriceOverride;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for a customer option price override.
 * Returned by all price-override admin endpoints.
 */
public record PriceOverrideResponse(
        Long id,
        Long customerId,
        Long optionId,
        BigDecimal customPrice,
        Instant createdAt,
        Instant updatedAt
) {
    public static PriceOverrideResponse from(CustomerOptionPriceOverride o) {
        return new PriceOverrideResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getOptionId(),
                o.getCustomPrice(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
