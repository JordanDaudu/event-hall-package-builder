package com.eventhall.controller;

import com.eventhall.dto.UpgradeDto;
import com.eventhall.service.UpgradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Exposes upgrade-related API endpoints.
@RestController
@RequestMapping("/api/upgrades")
public class UpgradeController {

    private final UpgradeService upgradeService;

    public UpgradeController(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    // Handles: GET /api/upgrades
    @GetMapping
    public List<UpgradeDto> getAllUpgrades() {
        return upgradeService.getAllUpgrades();
    }
}