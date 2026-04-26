package com.eventhall.dto;

import com.eventhall.enums.QuoteStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateQuoteStatusRequest(

        @NotNull(message = "Status is required")
        QuoteStatus status
) {}