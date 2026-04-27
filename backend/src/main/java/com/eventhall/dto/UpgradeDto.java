package com.eventhall.dto;

import java.math.BigDecimal;

/*
 * Response DTO for upgrades.
 *
 * The backend sends this to the frontend instead of sending the Upgrade entity.
 * This keeps the API clean and protects us from accidentally exposing database details.
 */
public record UpgradeDto(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        boolean active
) {}