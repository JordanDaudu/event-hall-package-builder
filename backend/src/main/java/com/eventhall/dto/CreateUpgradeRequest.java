package com.eventhall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/*
 * Request DTO for creating a new upgrade from the admin side.
 *
 * This is used by:
 * POST /api/admin/upgrades
 *
 * The admin does not send an id because the database generates the id.
 * The admin also does not send active because new upgrades are active by default
 * in the service layer.
 */
public record CreateUpgradeRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Category is required")
        String category,

        /*
         * BigDecimal is preferred for money instead of double.
         * double can have floating-point precision issues.
         * BigDecimal stores decimal values more safely for prices.
         */
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price
) {}