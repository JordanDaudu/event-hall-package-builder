package com.eventhall.service;

import com.eventhall.entity.EventType;
import com.eventhall.entity.Upgrade;
import com.eventhall.repository.EventTypeRepository;
import com.eventhall.repository.UpgradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EventTypeRepository eventTypeRepository;
    private final UpgradeRepository upgradeRepository;

    public DataInitializer(
            EventTypeRepository eventTypeRepository,
            UpgradeRepository upgradeRepository
    ) {
        this.eventTypeRepository = eventTypeRepository;
        this.upgradeRepository = upgradeRepository;
    }

    @Override
    public void run(String... args) {
        if (eventTypeRepository.count() == 0) {
            eventTypeRepository.save(new EventType("Wedding", BigDecimal.valueOf(120)));
            eventTypeRepository.save(new EventType("Birthday", BigDecimal.valueOf(80)));
            eventTypeRepository.save(new EventType("Corporate Event", BigDecimal.valueOf(100)));
            eventTypeRepository.save(new EventType("Bar/Bat Mitzvah", BigDecimal.valueOf(90)));
        }

        if (upgradeRepository.count() == 0) {
            upgradeRepository.save(new Upgrade("Flowers", "Premium flower arrangements", "Decor", BigDecimal.valueOf(2500), true));
            upgradeRepository.save(new Upgrade("DJ", "Professional DJ service", "Entertainment", BigDecimal.valueOf(3500), true));
            upgradeRepository.save(new Upgrade("Lighting", "Advanced hall lighting package", "Decor", BigDecimal.valueOf(1800), true));
            upgradeRepository.save(new Upgrade("Live Plants", "Natural live plant decoration", "Decor", BigDecimal.valueOf(1200), true));
            upgradeRepository.save(new Upgrade("Stage Design", "Custom designed event stage", "Stage", BigDecimal.valueOf(4000), true));
        }
    }
}