package com.eventhall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for creating or updating a customer price override.
 * Used by POST /api/admin/customers/{customerId}/price-overrides.
 */
public record PriceOverrideRequest(

        @NotNull(message = "optionId הוא שדה חובה")
        @Positive(message = "optionId חייב להיות מספר חיובי")
        Long optionId,

        @NotNull(message = "customPrice הוא שדה חובה")
        @DecimalMin(value = "0.0", inclusive = true, message = "המחיר חייב להיות אפס או יותר")
        BigDecimal customPrice
) {}
