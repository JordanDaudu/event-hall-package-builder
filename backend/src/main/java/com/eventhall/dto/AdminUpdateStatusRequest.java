package com.eventhall.dto;

import com.eventhall.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for PATCH /api/admin/requests/{id}/status.
 *
 * Only APPROVED and REJECTED are valid target statuses — PENDING is not
 * accepted (status can never be reset to PENDING after a decision).
 */
public record AdminUpdateStatusRequest(

        @NotNull(message = "סטטוס הוא שדה חובה")
        RequestStatus status,

        String summaryNotes
) {
}
