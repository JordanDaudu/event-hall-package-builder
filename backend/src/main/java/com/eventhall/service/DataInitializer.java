package com.eventhall.service;

import com.eventhall.entity.EventType;
import com.eventhall.entity.Upgrade;
import com.eventhall.repository.EventTypeRepository;
import com.eventhall.repository.UpgradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/*
 * DataInitializer seeds starter data into the database when the app starts.
 *
 * This is useful in the MVP because the frontend needs event types and upgrades
 * to already exist before a customer can build a package.
 *
 * @Component tells Spring to create this class as a Spring-managed object.
 */
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

    /*
     * CommandLineRunner.run(...) executes after the Spring application starts.
     *
     * This makes it a simple place to insert starter records into the database.
     * Later, professional projects often replace this with migrations such as Flyway.
     */
    @Override
    public void run(String... args) {
        /*
         * Only seed event types if the table is empty.
         * This prevents duplicate rows every time the app restarts.
         */
        if (eventTypeRepository.count() == 0) {
            eventTypeRepository.save(new EventType("Wedding", BigDecimal.valueOf(120)));
            eventTypeRepository.save(new EventType("Birthday", BigDecimal.valueOf(80)));
            eventTypeRepository.save(new EventType("Corporate Event", BigDecimal.valueOf(100)));
            eventTypeRepository.save(new EventType("Bar/Bat Mitzvah", BigDecimal.valueOf(90)));
        }

        /*
         * Only seed upgrades if the table is empty.
         * Each upgrade starts as active = true so customers can see it.
         */
        if (upgradeRepository.count() == 0) {
            upgradeRepository.save(new Upgrade("Flowers", "Premium flower arrangements", "Decor", BigDecimal.valueOf(2500), true));
            upgradeRepository.save(new Upgrade("DJ", "Professional DJ service", "Entertainment", BigDecimal.valueOf(3500), true));
            upgradeRepository.save(new Upgrade("Lighting", "Advanced hall lighting package", "Decor", BigDecimal.valueOf(1800), true));
            upgradeRepository.save(new Upgrade("Live Plants", "Natural live plant decoration", "Decor", BigDecimal.valueOf(1200), true));
            upgradeRepository.save(new Upgrade("Stage Design", "Custom designed event stage", "Stage", BigDecimal.valueOf(4000), true));
        }
    }
}