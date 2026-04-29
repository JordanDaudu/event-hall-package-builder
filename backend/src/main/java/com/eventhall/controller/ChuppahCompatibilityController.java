package com.eventhall.controller;

import com.eventhall.dto.*;
import com.eventhall.entity.OptionCompatibilityRule;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.OptionCompatibilityRuleRepository;
import com.eventhall.repository.PackageOptionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin endpoints for managing chuppah upgrade compatibility rules,
 * and one customer-facing endpoint that returns the full compatibility map.
 */
@RestController
public class ChuppahCompatibilityController {

    private final OptionCompatibilityRuleRepository ruleRepository;
    private final PackageOptionRepository optionRepository;

    public ChuppahCompatibilityController(
            OptionCompatibilityRuleRepository ruleRepository,
            PackageOptionRepository optionRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.optionRepository = optionRepository;
    }

    // -----------------------------------------------------------------------
    // Customer — full compatibility map
    // -----------------------------------------------------------------------

    /**
     * GET /api/chuppah-compatibility
     * Returns a map of chuppahId → list of allowed upgradeIds.
     * Used by the customer builder to filter upgrades.
     */
    @GetMapping("/api/chuppah-compatibility")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public Map<Long, List<Long>> getCompatibilityMap() {
        return ruleRepository.findAllByActiveTrue().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getParentOption().getId(),
                        Collectors.mapping(r -> r.getChildOption().getId(), Collectors.toList())
                ));
    }

    // -----------------------------------------------------------------------
    // Admin — list all chuppahs with their compatibility state
    // -----------------------------------------------------------------------

    /**
     * GET /api/admin/chuppah-compatibility
     * Returns all CHUPPAH options (for the admin to pick from).
     */
    @GetMapping("/api/admin/chuppah-compatibility")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PackageOptionResponse> listChuppahs() {
        return optionRepository.findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory.CHUPPAH)
                .stream().map(PackageOptionResponse::from).toList();
    }

    // -----------------------------------------------------------------------
    // Admin — get compatibility for one chuppah
    // -----------------------------------------------------------------------

    /**
     * GET /api/admin/chuppah-compatibility/{chuppahId}
     */
    @GetMapping("/api/admin/chuppah-compatibility/{chuppahId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ChuppahCompatibilityResponse getForChuppah(@PathVariable Long chuppahId) {
        PackageOption chuppah = requireChuppah(chuppahId);

        List<PackageOptionResponse> allUpgrades = optionRepository
                .findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory.CHUPPAH_UPGRADE)
                .stream().map(PackageOptionResponse::from).toList();

        List<Long> allowedIds = ruleRepository
                .findByParentOption_IdAndActiveTrue(chuppahId)
                .stream().map(r -> r.getChildOption().getId()).toList();

        return new ChuppahCompatibilityResponse(
                PackageOptionResponse.from(chuppah),
                allUpgrades,
                allowedIds
        );
    }

    // -----------------------------------------------------------------------
    // Admin — replace compatibility for one chuppah
    // -----------------------------------------------------------------------

    /**
     * PUT /api/admin/chuppah-compatibility/{chuppahId}
     * Replaces the set of allowed upgrades for the given chuppah.
     */
    @PutMapping("/api/admin/chuppah-compatibility/{chuppahId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ChuppahCompatibilityResponse setForChuppah(
            @PathVariable Long chuppahId,
            @Valid @RequestBody SetChuppahCompatibilityRequest req
    ) {
        PackageOption chuppah = requireChuppah(chuppahId);

        // Validate all provided upgrade ids are CHUPPAH_UPGRADE
        List<Long> upgradeIds = req.allowedUpgradeIds();
        List<PackageOption> upgrades = upgradeIds.isEmpty()
                ? List.of()
                : optionRepository.findAllById(upgradeIds);

        if (upgrades.size() != upgradeIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "אחד או יותר ממזהי תוספות החופה לא נמצא");
        }
        for (PackageOption u : upgrades) {
            if (u.getCategory() != PackageOptionCategory.CHUPPAH_UPGRADE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "\"" + u.getNameHe() + "\" אינה תוספת חופה");
            }
        }

        // Deactivate all existing rules for this chuppah
        List<OptionCompatibilityRule> existing = ruleRepository.findAllByParentOption_Id(chuppahId);
        for (OptionCompatibilityRule rule : existing) {
            rule.setActive(false);
        }
        ruleRepository.saveAll(existing);

        // Create new active rules
        for (PackageOption upgrade : upgrades) {
            // Reuse deactivated rule if exists, else create new
            OptionCompatibilityRule rule = existing.stream()
                    .filter(r -> r.getChildOption().getId().equals(upgrade.getId()))
                    .findFirst()
                    .orElse(OptionCompatibilityRule.builder()
                            .parentOption(chuppah)
                            .childOption(upgrade)
                            .build());
            rule.setActive(true);
            ruleRepository.save(rule);
        }

        // Return updated state
        return getForChuppah(chuppahId);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private PackageOption requireChuppah(Long id) {
        PackageOption opt = optionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "חופה לא נמצאה"));
        if (opt.getCategory() != PackageOptionCategory.CHUPPAH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "האפשרות שנבחרה אינה חופה ראשית");
        }
        return opt;
    }
}
