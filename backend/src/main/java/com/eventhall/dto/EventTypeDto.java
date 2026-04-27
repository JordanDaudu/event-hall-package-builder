package com.eventhall.dto;

import java.math.BigDecimal;

/*
 * Response DTO for event types.
 *
 * This is what the backend sends to the frontend when the frontend calls:
 * GET /api/event-types
 *
 * It contains only the fields the frontend needs for package building.
 */
public record EventTypeDto(
        Long id,
        String name,
        BigDecimal basePrice
) {}