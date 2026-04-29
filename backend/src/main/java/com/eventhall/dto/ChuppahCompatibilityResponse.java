package com.eventhall.dto;

import java.util.List;

/**
 * Returned by GET /api/admin/chuppah-compatibility/{chuppahId}.
 * Contains the selected chuppah, all available upgrades, and the currently allowed upgrade ids.
 */
public record ChuppahCompatibilityResponse(
        PackageOptionResponse chuppah,
        List<PackageOptionResponse> availableUpgrades,
        List<Long> allowedUpgradeIds
) {}
