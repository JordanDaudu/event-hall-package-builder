package com.eventhall.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body for PUT /api/admin/chuppah-compatibility/{chuppahId}.
 * Replaces the set of allowed upgrade ids for the given chuppah.
 */
public record SetChuppahCompatibilityRequest(
        @NotNull
        List<Long> allowedUpgradeIds
) {}
