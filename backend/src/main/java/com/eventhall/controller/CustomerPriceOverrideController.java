package com.eventhall.controller;

import com.eventhall.dto.PriceOverrideResponse;
import com.eventhall.entity.UserAccount;
import com.eventhall.repository.UserAccountRepository;
import com.eventhall.service.CustomerPriceOverrideService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * Customer-facing endpoint to retrieve the authenticated customer's own price overrides.
 *
 * GET /api/customer/my-overrides
 *
 * Returns a list of PriceOverrideResponse containing the optionId and customPrice
 * for every option that has a special price set by the admin for this customer.
 * The frontend package builder uses this to compute effective per-option prices.
 */
@RestController
@RequestMapping("/api/customer/my-overrides")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPriceOverrideController {

    private final CustomerPriceOverrideService priceOverrideService;
    private final UserAccountRepository userAccountRepository;

    public CustomerPriceOverrideController(
            CustomerPriceOverrideService priceOverrideService,
            UserAccountRepository userAccountRepository
    ) {
        this.priceOverrideService = priceOverrideService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public List<PriceOverrideResponse> getMyOverrides(Principal principal) {
        UserAccount customer = userAccountRepository
                .findByEmailIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "לקוח לא נמצא"));
        return priceOverrideService.listOverrides(customer.getId());
    }
}
