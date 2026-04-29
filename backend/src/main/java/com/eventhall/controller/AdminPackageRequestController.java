package com.eventhall.controller;

import com.eventhall.dto.AdminUpdateStatusRequest;
import com.eventhall.dto.PackageRequestDetailResponse;
import com.eventhall.dto.PackageRequestSummaryResponse;
import com.eventhall.enums.RequestStatus;
import com.eventhall.service.PackageRequestService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for viewing and deciding package requests.
 *
 * Protected by hasRole('ADMIN') via the SecurityConfig rule on /api/admin/**.
 */
@RestController
@RequestMapping("/api/admin/requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPackageRequestController {

    private final PackageRequestService packageRequestService;

    public AdminPackageRequestController(PackageRequestService packageRequestService) {
        this.packageRequestService = packageRequestService;
    }

    /**
     * List all requests, optionally filtered by status.
     *
     * @param status optional query param: PENDING, APPROVED, or REJECTED
     */
    @GetMapping
    public List<PackageRequestSummaryResponse> listAll(
            @RequestParam(required = false) RequestStatus status
    ) {
        return packageRequestService.listAll(status);
    }

    /**
     * Get full detail of a specific request.
     */
    @GetMapping("/{id}")
    public PackageRequestDetailResponse getById(@PathVariable Long id) {
        return packageRequestService.getByIdForAdmin(id);
    }

    /**
     * Approve or reject a request.
     * Returns 409 Conflict if the request has already been decided.
     * Returns 400 if the target status is PENDING (not a valid transition).
     */
    @PatchMapping("/{id}/status")
    public PackageRequestDetailResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateStatusRequest req
    ) {
        return packageRequestService.updateStatus(id, req);
    }
}
