package com.eventhall.dto;

import java.math.BigDecimal;

public record EventTypeRevenueDto(
        String eventTypeName,
        BigDecimal revenue
) {}