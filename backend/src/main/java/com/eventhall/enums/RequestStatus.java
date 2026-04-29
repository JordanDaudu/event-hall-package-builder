package com.eventhall.enums;

/**
 * Lifecycle states for a PackageRequest.
 *
 * Transitions:
 *   PENDING → APPROVED  (admin approves)
 *   PENDING → REJECTED  (admin rejects)
 * Once decided, the status is final — no transitions back to PENDING.
 */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
