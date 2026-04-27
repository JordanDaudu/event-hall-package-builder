package com.eventhall.controller;

import com.eventhall.dto.CreateUpgradeRequest;
import com.eventhall.dto.UpdateUpgradeRequest;
import com.eventhall.dto.UpgradeDto;
import com.eventhall.service.UpgradeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/*
 * Controller for admin upgrade management.
 *
 * Upgrades are extra options that can be added to an event package,
 * such as flowers, lighting, DJ, live plants, or stage design.
 *
 * This controller lets the admin:
 * - Create a new upgrade.
 * - Update an existing upgrade.
 * - Delete an upgrade using soft delete.
 *
 * "Soft delete" means we do not remove the row from the database.
 * We only mark active = false, so old quotes can still remember what upgrade existed.
 */
@RestController
@RequestMapping("/api/admin/upgrades")
public class AdminUpgradeController {

    /*
     * The controller delegates upgrade-related business logic to UpgradeService.
     * The service talks to the repository and handles entity-to-DTO conversion.
     */
    private final UpgradeService upgradeService;

    public AdminUpgradeController(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    /*
     * Handles:
     * POST /api/admin/upgrades
     *
     * This creates a new upgrade in the database.
     * The request body should contain fields like name, description, category, and price.
     */
    @PostMapping
    public UpgradeDto createUpgrade(@Valid @RequestBody CreateUpgradeRequest request) {
        return upgradeService.createUpgrade(request);
    }

    /*
     * Handles:
     * PUT /api/admin/upgrades/{id}
     *
     * This updates an existing upgrade.
     * Example URL: /api/admin/upgrades/3
     *
     * The id tells us which upgrade to update.
     * The body contains the new values.
     */
    @PutMapping("/{id}")
    public UpgradeDto updateUpgrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUpgradeRequest request
    ) {
        return upgradeService.updateUpgrade(id, request);
    }

    /*
     * Handles:
     * DELETE /api/admin/upgrades/{id}
     *
     * Even though this is a DELETE endpoint, the current implementation performs
     * a soft delete by setting active = false in the service layer.
     */
    @DeleteMapping("/{id}")
    public void deleteUpgrade(@PathVariable Long id) {
        upgradeService.deleteUpgrade(id);
    }
}