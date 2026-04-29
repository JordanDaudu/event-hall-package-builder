package com.eventhall.dto;

import com.eventhall.entity.UserAccount;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full customer detail view — returned by GET /api/admin/customers/{id}
 * and POST/PUT responses. Never includes passwordHash.
 */
public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String customerIdentityNumber,
        String phoneNumber,
        boolean active,
        BigDecimal basePackagePrice,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResponse from(UserAccount u) {
        return new CustomerResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getCustomerIdentityNumber(),
                u.getPhoneNumber(),
                u.isActive(),
                u.getBasePackagePrice(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
