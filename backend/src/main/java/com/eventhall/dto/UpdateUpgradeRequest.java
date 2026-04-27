package com.eventhall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/*
 * Request DTO for updating an existing upgrade.
 *
 * Used by:
 * PUT /api/admin/upgrades/{id}
 *
 * This includes active because the admin can enable or disable an upgrade.
 */
public record UpdateUpgradeRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        /*
         * Boolean instead of boolean allows validation to detect null.
         * If this were primitive boolean, Java would default it to false,
         * and we could not tell if the frontend forgot to send it.
         */
        @NotNull(message = "Active status is required")
        Boolean active
) {}