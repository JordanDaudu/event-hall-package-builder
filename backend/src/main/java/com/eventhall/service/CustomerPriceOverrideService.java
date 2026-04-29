package com.eventhall.service;

import com.eventhall.dto.PriceOverrideRequest;
import com.eventhall.dto.PriceOverrideResponse;
import com.eventhall.entity.CustomerOptionPriceOverride;
import com.eventhall.entity.PackageOption;
import com.eventhall.entity.UserAccount;
import com.eventhall.enums.UserRole;
import com.eventhall.repository.CustomerOptionPriceOverrideRepository;
import com.eventhall.repository.PackageOptionRepository;
import com.eventhall.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Admin-only service for managing per-customer price overrides on package options.
 *
 * Rules enforced:
 * - The target user must exist and have the CUSTOMER role.
 * - The referenced PackageOption must exist (real FK enforced at DB level too).
 * - Only one override per (customer, option) pair is allowed (DB unique constraint
 *   + upsert semantics in setOverride to avoid 409 on repeat POST).
 */
@Service
@Transactional
public class CustomerPriceOverrideService {

    private final CustomerOptionPriceOverrideRepository overrideRepository;
    private final UserAccountRepository userAccountRepository;
    private final PackageOptionRepository packageOptionRepository;

    public CustomerPriceOverrideService(
            CustomerOptionPriceOverrideRepository overrideRepository,
            UserAccountRepository userAccountRepository,
            PackageOptionRepository packageOptionRepository
    ) {
        this.overrideRepository = overrideRepository;
        this.userAccountRepository = userAccountRepository;
        this.packageOptionRepository = packageOptionRepository;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PriceOverrideResponse> listOverrides(Long customerId) {
        requireCustomer(customerId);
        return overrideRepository
                .findAllByCustomerIdOrderByPackageOption_IdAsc(customerId)
                .stream()
                .map(PriceOverrideResponse::from)
                .toList();
    }

    // -----------------------------------------------------------------------
    // Create / Update (upsert)
    // -----------------------------------------------------------------------

    public PriceOverrideResponse setOverride(Long customerId, PriceOverrideRequest req) {
        UserAccount customer = requireCustomer(customerId);
        PackageOption option = requirePackageOption(req.optionId());

        CustomerOptionPriceOverride override = overrideRepository
                .findByCustomerIdAndPackageOption_Id(customerId, req.optionId())
                .orElseGet(() -> CustomerOptionPriceOverride.builder()
                        .customer(customer)
                        .packageOption(option)
                        .build());

        override.setCustomPrice(req.customPrice());
        return PriceOverrideResponse.from(overrideRepository.save(override));
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteOverride(Long customerId, Long optionId) {
        requireCustomer(customerId);
        long deleted = overrideRepository.deleteByCustomerIdAndPackageOptionId(customerId, optionId);

        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "לא נמצא מחיר מיוחד עבור לקוח ואפשרות אלה");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private UserAccount requireCustomer(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "לקוח לא נמצא"));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "משתמש זה אינו לקוח");
        }
        return user;
    }

    private PackageOption requirePackageOption(Long optionId) {
        return packageOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "אפשרות חבילה לא נמצאה"));
    }
}
