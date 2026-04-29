package com.eventhall.dto;

import com.eventhall.enums.UserRole;

import java.math.BigDecimal;

/**
 * Public profile of the currently-authenticated user.
 * Returned by {@code GET /api/auth/me}.
 *
 * Never includes the password hash; only includes business fields the
 * frontend may legitimately need (full name, identity number for prefill,
 * phone, role, active flag, base package price).
 */
public record MeResponse(
        Long id,
        String fullName,
        String email,
        String customerIdentityNumber,
        String phoneNumber,
        UserRole role,
        boolean active,
        BigDecimal basePackagePrice
) {}
