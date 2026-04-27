package com.eventhall.service;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.dto.UpgradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/*
 * Service responsible only for price calculation.
 *
 * Keeping pricing in its own service is a good design choice because pricing rules
 * often grow over time. Later you may add:
 * - discounts
 * - tax
 * - seasonal prices
 * - minimum guest count rules
 * - different upgrade pricing types
 */
@Service
public class PricingService {

    /*
     * Final price formula:
     *
     * (event type base price per guest × guest count)
     * +
     * sum of selected upgrade flat prices
     *
     * Important backend rule:
     * The backend calculates the final price even if the frontend also shows
     * a live preview. Frontend calculations can be manipulated by users,
     * so the backend must be the source of truth.
     */
    public BigDecimal calculateTotal(
            EventTypeDto eventType,
            int guestCount,
            List<UpgradeDto> selectedUpgrades
    ) {
        /*
         * Calculate the base event price.
         * Example: Wedding basePrice 120 × 100 guests = 12000.
         */
        BigDecimal baseTotal = eventType.basePrice()
                .multiply(BigDecimal.valueOf(guestCount));

        /*
         * Calculate total upgrade cost.
         *
         * map(UpgradeDto::price) extracts the price from every upgrade.
         * reduce(BigDecimal.ZERO, BigDecimal::add) adds all prices together.
         */
        BigDecimal upgradesTotal = selectedUpgrades.stream()
                .map(UpgradeDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return baseTotal.add(upgradesTotal);
    }
}