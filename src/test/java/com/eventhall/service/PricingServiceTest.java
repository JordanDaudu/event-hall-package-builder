package com.eventhall.service;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.dto.UpgradeDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void calculateTotal_shouldAddBasePriceTimesGuestCountAndUpgradePrices() {
        EventTypeDto eventType = new EventTypeDto(
                1L,
                "Wedding",
                BigDecimal.valueOf(120)
        );

        List<UpgradeDto> upgrades = List.of(
                new UpgradeDto(1L, "Flowers", "Premium flowers", "Decor", BigDecimal.valueOf(2500), true),
                new UpgradeDto(2L, "DJ", "Professional DJ", "Entertainment", BigDecimal.valueOf(3500), true)
        );

        BigDecimal result = pricingService.calculateTotal(eventType, 100, upgrades);

        assertEquals(BigDecimal.valueOf(18000), result);
    }
}