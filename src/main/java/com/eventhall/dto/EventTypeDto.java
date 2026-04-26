package com.eventhall.dto;

import java.math.BigDecimal;

public record EventTypeDto(
        Long id,
        String name,
        BigDecimal basePrice
) {}