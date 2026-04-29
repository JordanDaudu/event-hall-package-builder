package com.eventhall.service;

import com.eventhall.entity.PackageOption;
import com.eventhall.repository.CustomerOptionPriceOverrideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Helper service that resolves the effective price for a package option
 * for a given customer.
 *
 * Resolution rule:
 *   1. If a CustomerOptionPriceOverride exists for (customerId, option.id), return its customPrice.
 *   2. Otherwise, return option.globalPrice.
 *
 * The method accepts a PackageOption entity directly so that Phase 7 (package
 * request submission) can pass the already-loaded entity without repeating the
 * look-up or passing the global price as a separate argument.
 */
@Service
@Transactional(readOnly = true)
public class PricingLookupService {

    private final CustomerOptionPriceOverrideRepository overrideRepository;

    public PricingLookupService(CustomerOptionPriceOverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    /**
     * Returns the effective price for the given customer and package option.
     *
     * @param customerId internal id of the CUSTOMER UserAccount
     * @param option     the PackageOption whose price is being resolved
     * @return customPrice from a CustomerOptionPriceOverride if one exists,
     *         otherwise option.getGlobalPrice()
     */
    public BigDecimal resolvePrice(Long customerId, PackageOption option) {
        return overrideRepository
                .findByCustomerIdAndPackageOption_Id(customerId, option.getId())
                .map(override -> override.getCustomPrice())
                .orElse(option.getGlobalPrice());
    }
}
