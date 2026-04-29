package com.eventhall.repository;

import com.eventhall.entity.CustomerOptionPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOptionPriceOverrideRepository extends JpaRepository<CustomerOptionPriceOverride, Long> {

    /**
     * Finds a specific override for the given customer and option pair.
     * Used by PricingLookupService to check whether a custom price exists.
     */
    Optional<CustomerOptionPriceOverride> findByCustomerIdAndOptionId(Long customerId, Long optionId);

    /**
     * Returns all overrides for a given customer, ordered by option id for
     * stable listing in the admin UI.
     */
    List<CustomerOptionPriceOverride> findAllByCustomerIdOrderByOptionId(Long customerId);

    /**
     * Checks existence without loading the full entity (used for conflict detection).
     */
    boolean existsByCustomerIdAndOptionId(Long customerId, Long optionId);

    /**
     * Deletes the override for a specific customer/option pair.
     * Returns the number of rows deleted (0 or 1).
     */
    long deleteByCustomerIdAndOptionId(Long customerId, Long optionId);
}
