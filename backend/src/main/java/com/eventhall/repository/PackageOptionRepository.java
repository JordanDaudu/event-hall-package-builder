package com.eventhall.repository;

import com.eventhall.entity.PackageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PackageOption.
 *
 * Phase 4 scaffold: only the minimal queries needed by
 * CustomerPriceOverrideService are declared here. Full query methods
 * (category filtering, active-only listing, search) will be added in Phase 6.
 */
@Repository
public interface PackageOptionRepository extends JpaRepository<PackageOption, Long> {

    /** Returns only active options, ordered for the builder UI. */
    List<PackageOption> findAllByActiveTrueOrderBySortOrderAsc();
}
