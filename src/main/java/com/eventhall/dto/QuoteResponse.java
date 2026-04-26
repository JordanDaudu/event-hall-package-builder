package com.eventhall.dto;

import com.eventhall.enums.QuoteStatus;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
        Long id,
        String eventTypeName,
        int guestCount,
        List<String> upgrades,
        BigDecimal totalPrice,
        QuoteStatus status
) {}