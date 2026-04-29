package com.eventhall.enums;

/**
 * Roles supported by the system.
 *
 * ADMIN     - hall manager / staff with full management access.
 * CUSTOMER  - end-user account created by an admin so a client can build packages.
 *
 * There is no public registration; CUSTOMER accounts are always created by an ADMIN.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER
}
