package com.eventhall.service;

import com.eventhall.dto.UpgradeDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

// @Service tells Spring:
// This class contains business logic.
// Create one object of it and manage it for me.
@Service
public class UpgradeService {

    private final List<UpgradeDto> upgrades = List.of(
            new UpgradeDto(1L, "Flowers", "Premium flower arrangements", "Decor", BigDecimal.valueOf(2500), true),
            new UpgradeDto(2L, "DJ", "Professional DJ service", "Entertainment", BigDecimal.valueOf(3500), true),
            new UpgradeDto(3L, "Lighting", "Advanced hall lighting package", "Decor", BigDecimal.valueOf(1800), true),
            new UpgradeDto(4L, "Live Plants", "Natural live plant decoration", "Decor", BigDecimal.valueOf(1200), true),
            new UpgradeDto(5L, "Stage Design", "Custom designed event stage", "Stage", BigDecimal.valueOf(4000), true)
    );

    public List<UpgradeDto> getAllUpgrades() {
        return upgrades;
    }

    public List<UpgradeDto> getUpgradesByIds(List<Long> ids) {
        return upgrades.stream()
                .filter(upgrade -> ids.contains(upgrade.id()))
                .toList();
    }
}