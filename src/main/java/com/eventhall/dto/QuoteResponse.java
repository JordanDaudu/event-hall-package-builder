package com.eventhall.dto;

import com.eventhall.enums.QuoteStatus;

import java.math.BigDecimal;
import java.util.List;

/*
 * Response DTO returned after quote operations.
 *
 * This is sent back to the frontend after:
 * - Creating a quote.
 * - Fetching a quote by id.
 * - Viewing quotes as admin.
 * - Updating quote status.
 *
 * Notice that this response does not expose full Customer, EventType,
 * QuoteItem, or Upgrade database entities. It gives a clean summary.
 */
public record QuoteResponse(
        Long id,
        String eventTypeName,
        int guestCount,
        List<String> upgrades,
        BigDecimal totalPrice,
        QuoteStatus status
) {}