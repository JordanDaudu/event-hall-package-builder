package com.eventhall.service;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.dto.UpgradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    /*
     * Final price formula:
     *
     * (event type base price per guest × guest count)
     * +
     * sum of selected upgrade flat prices
     */
    public BigDecimal calculateTotal(
            EventTypeDto eventType,
            int guestCount,
            List<UpgradeDto> selectedUpgrades
    ) {
        BigDecimal baseTotal = eventType.basePrice()
                .multiply(BigDecimal.valueOf(guestCount));

        BigDecimal upgradesTotal = selectedUpgrades.stream()
                .map(UpgradeDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return baseTotal.add(upgradesTotal);
    }
}