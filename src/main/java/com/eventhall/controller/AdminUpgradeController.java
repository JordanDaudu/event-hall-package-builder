package com.eventhall.controller;

import com.eventhall.dto.CreateUpgradeRequest;
import com.eventhall.dto.UpdateUpgradeRequest;
import com.eventhall.dto.UpgradeDto;
import com.eventhall.service.UpgradeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/upgrades")
public class AdminUpgradeController {

    private final UpgradeService upgradeService;

    public AdminUpgradeController(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @PostMapping
    public UpgradeDto createUpgrade(@Valid @RequestBody CreateUpgradeRequest request) {
        return upgradeService.createUpgrade(request);
    }

    @PutMapping("/{id}")
    public UpgradeDto updateUpgrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUpgradeRequest request
    ) {
        return upgradeService.updateUpgrade(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteUpgrade(@PathVariable Long id) {
        upgradeService.deleteUpgrade(id);
    }
}