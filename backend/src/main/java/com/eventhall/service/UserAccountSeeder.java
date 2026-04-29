package com.eventhall.service;

import com.eventhall.entity.UserAccount;
import com.eventhall.enums.UserRole;
import com.eventhall.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a single default ADMIN account at startup if no admin exists yet.
 *
 * Credentials are sourced from {@code ADMIN_EMAIL} / {@code ADMIN_PASSWORD}
 * environment variables. If those are missing, dev-only fallback values from
 * {@code application.properties} are used and the chosen credentials are
 * logged so the developer can sign in.
 */
@Component
@Order(1)
public class UserAccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserAccountSeeder.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String defaultEmail;
    private final String defaultPassword;
    private final String defaultFullName;

    public UserAccountSeeder(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.admin.default-email}") String defaultEmail,
            @Value("${app.admin.default-password}") String defaultPassword,
            @Value("${app.admin.default-full-name}") String defaultFullName
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.defaultEmail = defaultEmail;
        this.defaultPassword = defaultPassword;
        this.defaultFullName = defaultFullName;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userAccountRepository.existsByRole(UserRole.ADMIN)) {
            log.info("[UserAccountSeeder] At least one ADMIN already exists, skipping seed.");
            return;
        }

        if (userAccountRepository.existsByEmailIgnoreCase(defaultEmail)) {
            log.warn("[UserAccountSeeder] No ADMIN exists but a non-admin user with email {} is already taken. Skipping seed.", defaultEmail);
            return;
        }

        // Refuse to seed in production unless ADMIN_EMAIL and ADMIN_PASSWORD
        // were explicitly provided via environment variables (no insecure fallbacks).
        boolean isProd = environment.matchesProfiles("prod");
        boolean envProvidedEmail = System.getenv("ADMIN_EMAIL") != null;
        boolean envProvidedPassword = System.getenv("ADMIN_PASSWORD") != null;
        if (isProd && (!envProvidedEmail || !envProvidedPassword)) {
            log.error("[UserAccountSeeder] Refusing to seed default ADMIN in 'prod' profile without explicit ADMIN_EMAIL and ADMIN_PASSWORD env vars.");
            return;
        }

        UserAccount admin = UserAccount.builder()
                .fullName(defaultFullName)
                .email(defaultEmail)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        userAccountRepository.save(admin);

        // Never log the password value. If the dev fallback was used, point
        // the developer at the environment variables instead.
        log.info("==============================================================");
        log.info(" Seeded initial ADMIN account: {}", defaultEmail);
        if (!envProvidedPassword) {
            log.warn(" Using DEV-ONLY default password from application.properties.");
            log.warn(" Set ADMIN_EMAIL and ADMIN_PASSWORD env vars to override.");
        }
        log.info("==============================================================");
    }
}
