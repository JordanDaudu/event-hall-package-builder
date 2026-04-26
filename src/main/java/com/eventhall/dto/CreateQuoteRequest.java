package com.eventhall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuoteRequest(

        @NotNull(message = "Event type is required")
        Long eventTypeId,

        @Min(value = 1, message = "Guest count must be at least 1")
        int guestCount,

        @NotNull(message = "Upgrade IDs list is required")
        List<Long> upgradeIds,

        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        String customerEmail
) {}