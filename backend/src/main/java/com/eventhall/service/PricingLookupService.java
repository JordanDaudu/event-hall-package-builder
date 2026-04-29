package com.eventhall.service;

import com.eventhall.repository.CustomerOptionPriceOverrideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Helper service that resolves the effective price for a package option
 * for a given customer.
 *
 * Resolution rule:
 *   1. If a CustomerOptionPriceOverride exists for (customerId, optionId), return its customPrice.
 *   2. Otherwise, return the globalPrice passed in by the caller.
 *
 * This service is intentionally thin — it contains only the look-up logic
 * and does not know about the full package or quote domain. It will be
 * consumed by the package-request flow in Phase 7.
 */
@Service
@Transactional(readOnly = true)
public class PricingLookupService {

    private final CustomerOptionPriceOverrideRepository overrideRepository;

    public PricingLookupService(CustomerOptionPriceOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    /**
     * Returns the effective price for the given customer and option.
     *
     * @param customerId  internal id of the CUSTOMER UserAccount
     * @param optionId    id of the PackageOption (forward reference to Phase 6)
     * @param globalPrice the price defined on the PackageOption itself
     * @return customPrice if an override exists, otherwise globalPrice
     */
    public BigDecimal resolvePrice(Long customerId, Long optionId, BigDecimal globalPrice) {
        return overrideRepository
                .findByCustomerIdAndOptionId(customerId, optionId)
                .map(override -> override.getCustomPrice())
                .orElse(globalPrice);
    }
}
