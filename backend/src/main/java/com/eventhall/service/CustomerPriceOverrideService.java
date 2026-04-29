package com.eventhall.service;

import com.eventhall.dto.PriceOverrideRequest;
import com.eventhall.dto.PriceOverrideResponse;
import com.eventhall.entity.CustomerOptionPriceOverride;
import com.eventhall.entity.UserAccount;
import com.eventhall.enums.UserRole;
import com.eventhall.repository.CustomerOptionPriceOverrideRepository;
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
 * - Only one override per (customer, option) pair is allowed (enforced by DB unique constraint
 *   and checked here to give a friendly 409 instead of a raw DB error).
 * - SET is upsert-style: create if absent, update customPrice if present.
 */
@Service
@Transactional
public class CustomerPriceOverrideService {

    private final CustomerOptionPriceOverrideRepository overrideRepository;
    private final UserAccountRepository userAccountRepository;

    public CustomerPriceOverrideService(
            CustomerOptionPriceOverrideRepository overrideRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.overrideRepository = overrideRepository;
        this.userAccountRepository = userAccountRepository;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PriceOverrideResponse> listOverrides(Long customerId) {
        requireCustomer(customerId);
        return overrideRepository
                .findAllByCustomerIdOrderByOptionId(customerId)
                .stream()
                .map(PriceOverrideResponse::from)
                .toList();
    }

    // -----------------------------------------------------------------------
    // Create / Update (upsert)
    // -----------------------------------------------------------------------

    public PriceOverrideResponse setOverride(Long customerId, PriceOverrideRequest req) {
        UserAccount customer = requireCustomer(customerId);

        CustomerOptionPriceOverride override = overrideRepository
                .findByCustomerIdAndOptionId(customerId, req.optionId())
                .orElseGet(() -> CustomerOptionPriceOverride.builder()
                        .customer(customer)
                        .optionId(req.optionId())
                        .build());

        override.setCustomPrice(req.customPrice());
        return PriceOverrideResponse.from(overrideRepository.save(override));
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteOverride(Long customerId, Long optionId) {
        requireCustomer(customerId);
        long deleted = overrideRepository.deleteByCustomerIdAndOptionId(customerId, optionId);
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
}
