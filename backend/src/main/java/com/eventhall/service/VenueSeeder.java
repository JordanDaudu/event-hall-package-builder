package com.eventhall.service;

import com.eventhall.entity.Venue;
import com.eventhall.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds two default venues at startup if no venues exist.
 * Runs after UserAccountSeeder (Order 2).
 */
@Component
@Order(2)
public class VenueSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VenueSeeder.class);

    private final VenueRepository venueRepository;

    public VenueSeeder(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (venueRepository.count() > 0) {
            log.info("[VenueSeeder] Venues already exist, skipping seed.");
            return;
        }

        List<Venue> defaults = List.of(
                Venue.builder()
                        .nameHe("אולם הגן")
                        .nameEn("Garden Hall")
                        .descriptionHe("אולם מרשים בסגנון גן עם חציר ירוק ונוף פנורמי")
                        .imageUrl(null)
                        .active(true)
                        .sortOrder(1)
                        .build(),
                Venue.builder()
                        .nameHe("אולם הכוכבים")
                        .nameEn("Stars Hall")
                        .descriptionHe("אולם יוקרתי עם תקרת כוכבים ותאורת קריסטל")
                        .imageUrl(null)
                        .active(true)
                        .sortOrder(2)
                        .build()
        );

        venueRepository.saveAll(defaults);
        log.info("[VenueSeeder] Seeded {} default venues.", defaults.size());
    }
}
