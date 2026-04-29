package com.eventhall.controller;

import com.eventhall.dto.*;
import com.eventhall.service.PackageOptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only REST API for managing event package options.
 *
 * Endpoints:
 *   GET    /api/admin/package-options         → list all (incl. inactive)
 *   GET    /api/admin/package-options/{id}    → get one
 *   POST   /api/admin/package-options         → create
 *   PUT    /api/admin/package-options/{id}    → update (PATCH semantics — null fields ignored)
 *   PATCH  /api/admin/package-options/{id}/active → enable / disable
 *   DELETE /api/admin/package-options/{id}    → soft-delete (sets active=false)
 */
@RestController
@RequestMapping("/api/admin/package-options")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPackageOptionController {

    private final PackageOptionService packageOptionService;

    public AdminPackageOptionController(PackageOptionService packageOptionService) {
        this.packageOptionService = packageOptionService;
    }

    @GetMapping
    public List<PackageOptionResponse> listAll(
            @RequestParam(required = false) PackageOptionCategory category
    ) {
        return category != null
                ? packageOptionService.listAll().stream()
                    .filter(o -> o.category() == category).toList()
                : packageOptionService.listAll();
    }

    @GetMapping("/{id}")
    public PackageOptionResponse getById(@PathVariable Long id) {
        return packageOptionService.getById(id);
    }

    @PostMapping
    public ResponseEntity<PackageOptionResponse> create(
            @Valid @RequestBody CreatePackageOptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(packageOptionService.create(request));
    }

    @PutMapping("/{id}")
    public PackageOptionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePackageOptionRequest request
    ) {
        return packageOptionService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public PackageOptionResponse setActive(
            @PathVariable Long id,
            @Valid @RequestBody SetActiveRequest request
    ) {
        return packageOptionService.setActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        packageOptionService.delete(id);
        return ResponseEntity.ok(Map.of("message", "אפשרות החבילה הוסרה בהצלחה"));
    }
}
