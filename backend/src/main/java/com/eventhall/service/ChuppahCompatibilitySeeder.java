package com.eventhall.service;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.entity.OptionCompatibilityRule;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.OptionCompatibilityRuleRepository;
import com.eventhall.repository.PackageOptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds chuppah upgrade options and demo compatibility rules.
 * Runs after PackageOptionSeeder (@Order 3).
 *
 * Also sets the visualBehavior field on existing CHUPPAH options.
 */
@Component
@Order(4)
public class ChuppahCompatibilitySeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ChuppahCompatibilitySeeder.class);

    private final PackageOptionRepository optionRepository;
    private final OptionCompatibilityRuleRepository ruleRepository;

    public ChuppahCompatibilitySeeder(PackageOptionRepository optionRepository,
                                      OptionCompatibilityRuleRepository ruleRepository) {
        this.optionRepository = optionRepository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // ── 1. Ensure CHUPPAH options have visualBehavior set ─────────────
        List<PackageOption> chuppahs = optionRepository
                .findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory.CHUPPAH);
        for (PackageOption c : chuppahs) {
            if (c.getVisualBehavior() == null) {
                c.setVisualBehavior("REPLACE_IMAGE");
                optionRepository.save(c);
            }
        }

        // ── 2. Seed CHUPPAH_UPGRADE options if none exist ─────────────────
        List<PackageOption> existingUpgrades = optionRepository
                .findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory.CHUPPAH_UPGRADE);

        if (!existingUpgrades.isEmpty()) {
            log.info("ChuppahCompatibilitySeeder: upgrades already seeded, skipping.");
            ensureCompatibilityRules(chuppahs,
                    optionRepository.findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory.CHUPPAH_UPGRADE));
            return;
        }

        log.info("ChuppahCompatibilitySeeder: seeding chuppah upgrade options and compatibility rules.");

        PackageOption flowers = optionRepository.save(PackageOption.builder()
                .nameHe("תוספת פרחים לחופה")
                .nameEn("Flower Addition")
                .category(PackageOptionCategory.CHUPPAH_UPGRADE)
                .globalPrice(BigDecimal.valueOf(800))
                .visualBehavior("OVERLAY_IMAGE")
                .sortOrder(1)
                .build());

        PackageOption lighting = optionRepository.save(PackageOption.builder()
                .nameHe("תוספת תאורה לחופה")
                .nameEn("Lighting Addition")
                .category(PackageOptionCategory.CHUPPAH_UPGRADE)
                .globalPrice(BigDecimal.valueOf(600))
                .visualBehavior("OVERLAY_IMAGE")
                .sortOrder(2)
                .build());

        PackageOption greenery = optionRepository.save(PackageOption.builder()
                .nameHe("תוספת ירק לחופה")
                .nameEn("Greenery Addition")
                .category(PackageOptionCategory.CHUPPAH_UPGRADE)
                .globalPrice(BigDecimal.valueOf(500))
                .visualBehavior("OVERLAY_IMAGE")
                .sortOrder(3)
                .build());

        PackageOption fabric = optionRepository.save(PackageOption.builder()
                .nameHe("תוספת בד לחופה")
                .nameEn("Fabric Addition")
                .category(PackageOptionCategory.CHUPPAH_UPGRADE)
                .globalPrice(BigDecimal.valueOf(400))
                .visualBehavior("OVERLAY_IMAGE")
                .sortOrder(4)
                .build());

        List<PackageOption> upgrades = List.of(flowers, lighting, greenery, fabric);

        // ── 3. Build compatibility rules ──────────────────────────────────
        // Sort chuppahs by id for deterministic assignment
        List<PackageOption> sortedChuppahs = chuppahs.stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();

        if (sortedChuppahs.size() >= 1) {
            // חופה לבנה קלאסית → תאורה only
            addRule(sortedChuppahs.get(0), lighting);
        }
        if (sortedChuppahs.size() >= 2) {
            // חופה ירוקייה → ירק + תאורה
            addRule(sortedChuppahs.get(1), greenery);
            addRule(sortedChuppahs.get(1), lighting);
        }
        if (sortedChuppahs.size() >= 3) {
            // חופה פרחונית מלאה → פרחים + תאורה + בד
            addRule(sortedChuppahs.get(2), flowers);
            addRule(sortedChuppahs.get(2), lighting);
            addRule(sortedChuppahs.get(2), fabric);
        }
        if (sortedChuppahs.size() >= 4) {
            // חופה בוהו שיק → כולם
            for (PackageOption u : upgrades) {
                addRule(sortedChuppahs.get(3), u);
            }
        }

        log.info("ChuppahCompatibilitySeeder: done.");
    }

    private void ensureCompatibilityRules(List<PackageOption> chuppahs, List<PackageOption> upgrades) {
        if (ruleRepository.count() > 0 || chuppahs.isEmpty() || upgrades.isEmpty()) {
            return;
        }
        // If we have upgrades but no rules (e.g. upgrades were seeded but rules were dropped),
        // re-create the rules from scratch using the same pattern.
        List<PackageOption> sortedChuppahs = chuppahs.stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
        List<PackageOption> sortedUpgrades = upgrades.stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();

        if (sortedUpgrades.size() < 4 || sortedChuppahs.isEmpty()) return;

        PackageOption flowers = sortedUpgrades.get(0);
        PackageOption lighting = sortedUpgrades.get(1);
        PackageOption greenery = sortedUpgrades.get(2);
        PackageOption fabric = sortedUpgrades.get(3);

        if (sortedChuppahs.size() >= 1) addRule(sortedChuppahs.get(0), lighting);
        if (sortedChuppahs.size() >= 2) { addRule(sortedChuppahs.get(1), greenery); addRule(sortedChuppahs.get(1), lighting); }
        if (sortedChuppahs.size() >= 3) { addRule(sortedChuppahs.get(2), flowers); addRule(sortedChuppahs.get(2), lighting); addRule(sortedChuppahs.get(2), fabric); }
        if (sortedChuppahs.size() >= 4) { for (PackageOption u : sortedUpgrades) addRule(sortedChuppahs.get(3), u); }
    }

    private void addRule(PackageOption parent, PackageOption child) {
        if (!ruleRepository.existsByParentOption_IdAndChildOption_IdAndActiveTrue(
                parent.getId(), child.getId())) {
            ruleRepository.save(OptionCompatibilityRule.builder()
                    .parentOption(parent)
                    .childOption(child)
                    .active(true)
                    .build());
        }
    }
}
