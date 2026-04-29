package com.eventhall.controller;

import com.eventhall.dto.PackageRequestDetailResponse;
import com.eventhall.dto.PackageRequestSummaryResponse;
import com.eventhall.dto.SubmitRequestRequest;
import com.eventhall.service.PackageRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Customer-facing package request endpoints.
 *
 * All endpoints are protected by hasRole('CUSTOMER') via the SecurityConfig
 * rule on /api/customer/**.
 * The authenticated customer's identity is always extracted from the JWT principal;
 * it is never accepted from a request body or query parameter.
 */
@RestController
@RequestMapping("/api/customer/requests")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPackageRequestController {

    private final PackageRequestService packageRequestService;

    public CustomerPackageRequestController(PackageRequestService packageRequestService) {
        this.packageRequestService = packageRequestService;
    }

    /**
     * Submit a new package request.
     * Returns 201 Created with the full detail of the saved request.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PackageRequestDetailResponse submit(
            Principal principal,
            @Valid @RequestBody SubmitRequestRequest req
    ) {
        return packageRequestService.submit(principal.getName(), req);
    }

    /**
     * List all requests belonging to the authenticated customer (summary view).
     */
    @GetMapping
    public List<PackageRequestSummaryResponse> list(Principal principal) {
        return packageRequestService.listByCustomer(principal.getName());
    }

    /**
     * Get full detail of a specific request. Returns 404 if the request
     * does not exist or does not belong to the authenticated customer.
     */
    @GetMapping("/{id}")
    public PackageRequestDetailResponse getById(
            Principal principal,
            @PathVariable Long id
    ) {
        return packageRequestService.getByIdForCustomer(principal.getName(), id);
    }
}
