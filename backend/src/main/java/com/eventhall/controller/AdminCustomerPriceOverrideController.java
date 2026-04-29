package com.eventhall.controller;

import com.eventhall.dto.PriceOverrideRequest;
import com.eventhall.dto.PriceOverrideResponse;
import com.eventhall.service.CustomerPriceOverrideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only REST API for managing per-customer package option price overrides.
 *
 * All endpoints require the ADMIN role (enforced globally by SecurityConfig for
 * /api/admin/** and also locally via @PreAuthorize for extra clarity).
 *
 * Endpoints:
 *   GET    /api/admin/customers/{customerId}/price-overrides
 *       → List all overrides for a customer
 *
 *   POST   /api/admin/customers/{customerId}/price-overrides
 *       → Create or update (upsert) an override for a specific option
 *
 *   DELETE /api/admin/customers/{customerId}/price-overrides/{optionId}
 *       → Remove the override for a specific option (falls back to global price)
 */
@RestController
@RequestMapping("/api/admin/customers/{customerId}/price-overrides")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerPriceOverrideController {

    private final CustomerPriceOverrideService priceOverrideService;

    public AdminCustomerPriceOverrideController(CustomerPriceOverrideService priceOverrideService) {
        this.priceOverrideService = priceOverrideService;
    }

    @GetMapping
    public List<PriceOverrideResponse> listOverrides(@PathVariable Long customerId) {
        return priceOverrideService.listOverrides(customerId);
    }

    @PostMapping
    public ResponseEntity<PriceOverrideResponse> setOverride(
            @PathVariable Long customerId,
            @Valid @RequestBody PriceOverrideRequest request
    ) {
        PriceOverrideResponse result = priceOverrideService.setOverride(customerId, request);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Map<String, String>> deleteOverride(
            @PathVariable Long customerId,
            @PathVariable Long optionId
    ) {
        priceOverrideService.deleteOverride(customerId, optionId);
        return ResponseEntity.ok(Map.of("message", "המחיר המיוחד נמחק בהצלחה"));
    }
}
