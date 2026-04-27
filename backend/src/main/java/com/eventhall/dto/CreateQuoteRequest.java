package com.eventhall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/*
 * DTO = Data Transfer Object.
 *
 * This record represents the JSON body the frontend sends when a customer
 * submits a quote request.
 *
 * We use a DTO instead of exposing the Quote entity directly because:
 * - The frontend should only send the data needed for this action.
 * - The database entity has relationships and fields the frontend should not control.
 * - Validation rules can be placed directly on request fields.
 *
 * A Java record is a compact way to create an immutable data carrier.
 * Java automatically creates the constructor and getter-like methods.
 * For example, eventTypeId() is generated automatically.
 */
public record CreateQuoteRequest(

        @NotNull(message = "Event type is required")
        Long eventTypeId,

        @Min(value = 1, message = "Guest count must be at least 1")
        int guestCount,

        /*
         * The list itself cannot be null.
         *
         * Example valid values:
         * "upgradeIds": []
         * "upgradeIds": [1, 2, 3]
         *
         * Empty list is allowed because a customer may choose no upgrades.
         */
        @NotNull(message = "Upgrade IDs list is required")
        List<@NotNull(message = "Upgrade ID cannot be null") Long> upgradeIds,

        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        String customerEmail,

        @NotBlank(message = "Customer phone number is required")
        @Pattern(
                regexp = "^$|^[0-9+\\-\\s]{7,20}$",
                message = "Customer phone number must be valid"
        )
        String customerPhoneNumber
) {}