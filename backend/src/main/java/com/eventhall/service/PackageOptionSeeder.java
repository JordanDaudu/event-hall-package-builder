package com.eventhall.service;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.PackageOptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a small default catalog of package options at startup if none exist.
 * Runs after UserAccountSeeder (@Order 1) and VenueSeeder (@Order 2).
 */
@Component
@Order(3)
public class PackageOptionSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PackageOptionSeeder.class);

    private final PackageOptionRepository optionRepository;

    public PackageOptionSeeder(PackageOptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (optionRepository.count() > 0) {
            return;
        }

        List<PackageOption> defaults = List.of(
                // ── Catering ────────────────────────────────────────────────
                PackageOption.builder()
                        .nameHe("מנה לאורח")
                        .nameEn("Per-Guest Meal")
                        .category(PackageOptionCategory.CATERING)
                        .globalPrice(new BigDecimal("250.00"))
                        .active(true)
                        .sortOrder(10)
                        .build(),
                PackageOption.builder()
                        .nameHe("חבילת אלכוהול פרימיום")
                        .nameEn("Premium Alcohol Package")
                        .category(PackageOptionCategory.CATERING)
                        .globalPrice(new BigDecimal("80.00"))
                        .active(true)
                        .sortOrder(20)
                        .build(),

                // ── Decoration ───────────────────────────────────────────────
                PackageOption.builder()
                        .nameHe("זר פרחים לשולחן")
                        .nameEn("Table Flower Centerpiece")
                        .category(PackageOptionCategory.DECORATION)
                        .globalPrice(new BigDecimal("350.00"))
                        .active(true)
                        .sortOrder(30)
                        .build(),
                PackageOption.builder()
                        .nameHe("קישוט בלונים")
                        .nameEn("Balloon Decoration")
                        .category(PackageOptionCategory.DECORATION)
                        .globalPrice(new BigDecimal("500.00"))
                        .active(true)
                        .sortOrder(40)
                        .build(),

                // ── Music ────────────────────────────────────────────────────
                PackageOption.builder()
                        .nameHe("DJ מקצועי")
                        .nameEn("Professional DJ")
                        .category(PackageOptionCategory.MUSIC)
                        .globalPrice(new BigDecimal("3000.00"))
                        .active(true)
                        .sortOrder(50)
                        .build(),

                // ── Photography ──────────────────────────────────────────────
                PackageOption.builder()
                        .nameHe("צלם מקצועי")
                        .nameEn("Professional Photographer")
                        .category(PackageOptionCategory.PHOTOGRAPHY)
                        .globalPrice(new BigDecimal("5000.00"))
                        .active(true)
                        .sortOrder(60)
                        .build(),
                PackageOption.builder()
                        .nameHe("צילום וידאו")
                        .nameEn("Video Recording")
                        .category(PackageOptionCategory.PHOTOGRAPHY)
                        .globalPrice(new BigDecimal("3500.00"))
                        .active(true)
                        .sortOrder(70)
                        .build(),

                // ── Extras ───────────────────────────────────────────────────
                PackageOption.builder()
                        .nameHe("שירות חניה")
                        .nameEn("Valet Parking")
                        .category(PackageOptionCategory.EXTRAS)
                        .globalPrice(new BigDecimal("1200.00"))
                        .active(true)
                        .sortOrder(80)
                        .build()
        );

        optionRepository.saveAll(defaults);
        log.info("PackageOptionSeeder: seeded {} default package options", defaults.size());
    }
}
