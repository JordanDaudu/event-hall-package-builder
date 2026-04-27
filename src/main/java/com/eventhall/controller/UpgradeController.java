package com.eventhall.controller;

import com.eventhall.dto.UpgradeDto;
import com.eventhall.service.UpgradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Public upgrade controller.
 *
 * This is used by the customer package builder page.
 * Customers should only see active upgrades.
 * Inactive upgrades are hidden because they were soft-deleted or disabled by admin.
 */
@RestController
@RequestMapping("/api/upgrades")
public class UpgradeController {

    private final UpgradeService upgradeService;

    public UpgradeController(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    /*
     * Handles:
     * GET /api/upgrades
     *
     * Returns active upgrades as DTOs.
     * The frontend uses this list to show selectable package add-ons.
     */
    @GetMapping
    public List<UpgradeDto> getAllUpgrades() {
        return upgradeService.getAllUpgrades();
    }
}