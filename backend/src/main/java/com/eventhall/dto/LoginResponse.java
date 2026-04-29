package com.eventhall.dto;

import com.eventhall.enums.UserRole;

/**
 * Response returned after a successful login: the signed JWT plus the
 * essential user information needed by the frontend to build its UI.
 *
 * The password hash is never included.
 */
public record LoginResponse(
        String token,
        long expiresInMinutes,
        Long userId,
        String fullName,
        String email,
        UserRole role
) {}
