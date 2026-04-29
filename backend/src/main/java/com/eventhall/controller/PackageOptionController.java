package com.eventhall.controller;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.dto.PackageOptionResponse;
import com.eventhall.service.PackageOptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public (no auth required) package option endpoints.
 * Exposes only active options so the customer builder can populate its catalog
 * before the user is fully authenticated.
 */
@RestController
@RequestMapping("/api/package-options")
public class PackageOptionController {

    private final PackageOptionService packageOptionService;

    public PackageOptionController(PackageOptionService packageOptionService) {
        this.packageOptionService = packageOptionService;
    }

    /**
     * Returns all active options, optionally filtered by category.
     * Sorted by sortOrder ascending.
     *
     * GET /api/package-options
     * GET /api/package-options?category=CATERING
     */
    @GetMapping
    public List<PackageOptionResponse> listActive(
            @RequestParam(required = false) PackageOptionCategory category
    ) {
        return category != null
                ? packageOptionService.listActiveByCategory(category)
                : packageOptionService.listActive();
    }
}
