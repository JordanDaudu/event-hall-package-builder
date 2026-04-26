package com.eventhall.dto;

import java.math.BigDecimal;

public record UpgradeDto(
        Long id,
        String name,
        String description,
        String category,
        BigDecimal price,
        boolean active
) {}