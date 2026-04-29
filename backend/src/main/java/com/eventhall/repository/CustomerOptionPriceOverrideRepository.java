package com.eventhall.repository;

import com.eventhall.entity.CustomerOptionPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOptionPriceOverrideRepository extends JpaRepository<CustomerOptionPriceOverride, Long> {

    /**
     * Finds a specific override for the given customer and package-option pair.
     *
     * Spring Data JPA traverses associations using underscore notation:
     * "PackageOption_Id" means "packageOption.id", removing ambiguity with a
     * hypothetical plain field named "packageOptionId".
     */
    Optional<CustomerOptionPriceOverride> findByCustomerIdAndPackageOption_Id(
            Long customerId, Long packageOptionId);

    /**
     * Returns all overrides for a given customer, ordered by option id for
     * stable listing in the admin UI.
     */
    List<CustomerOptionPriceOverride> findAllByCustomerIdOrderByPackageOption_IdAsc(Long customerId);

    /**
     * Checks existence without loading the full entity.
     */
    boolean existsByCustomerIdAndPackageOption_Id(Long customerId, Long packageOptionId);

    /**
     * Deletes the override for a specific customer/option pair.
     * Uses an explicit @Query because Spring Data cannot always generate
     * DELETE statements for traversed associations.
     * Returns the number of rows deleted (0 or 1).
     */
    @Modifying
    @Query("DELETE FROM CustomerOptionPriceOverride c " +
           "WHERE c.customer.id = :customerId AND c.packageOption.id = :packageOptionId")
    long deleteByCustomerIdAndPackageOptionId(
            @Param("customerId") Long customerId,
            @Param("packageOptionId") Long packageOptionId);
}
