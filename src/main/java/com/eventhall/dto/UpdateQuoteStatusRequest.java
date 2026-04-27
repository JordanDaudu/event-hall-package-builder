package com.eventhall.dto;

import com.eventhall.enums.QuoteStatus;
import jakarta.validation.constraints.NotNull;

/*
 * Request DTO for changing a quote status.
 *
 * Used by:
 * PUT /api/admin/quotes/{id}/status
 *
 * Example JSON:
 * {
 *   "status": "CONTACTED"
 * }
 */
public record UpdateQuoteStatusRequest(

        /*
         * The status must be one of the values defined in QuoteStatus enum:
         * NEW, CONTACTED, APPROVED, REJECTED.
         *
         * If the frontend sends an invalid value like "DONE",
         * Spring cannot convert it to QuoteStatus and the global exception handler
         * returns a clean bad request response.
         */
        @NotNull(message = "Status is required")
        QuoteStatus status
) {}