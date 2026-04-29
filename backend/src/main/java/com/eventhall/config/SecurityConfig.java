package com.eventhall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the JWT-based REST API.
 *
 * Highlights:
 * - Stateless (no HTTP session, no CSRF — REST API only).
 * - {@code POST /api/auth/login} is open; everything else under {@code /api/auth/**}
 *   requires authentication.
 * - {@code /api/admin/**} requires the ADMIN role.
 * - {@code /api/customer/**} requires the CUSTOMER role.
 * - During the migration, the legacy public endpoints (event-types, upgrades,
 *   quotes, config) remain {@code permitAll} so the existing UI keeps working
 *   while the new backend is built out. They will be removed in later phases.
 * - Swagger UI / OpenAPI docs remain open for development.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // We use Bearer JWTs in the Authorization header (no cookies), so we
        // intentionally keep credentials disabled and pair that with a wide
        // origin allowlist. This is safe for a JWT-only API and avoids the
        // "Access-Control-Allow-Origin: * with credentials" anti-pattern.
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Always allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public auth endpoint
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()

                        // Swagger / OpenAPI (kept open for development)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Role-based areas (will fill out as new endpoints are added)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")

                        // Health / actuator-style root
                        .requestMatchers("/", "/error").permitAll()

                        // Public venue listing — venue names/descriptions contain no
                        // sensitive data; making this endpoint open simplifies the
                        // customer builder (no need to defer until after login).
                        .requestMatchers(HttpMethod.GET, "/api/venues").permitAll()

                        // Public package-option catalog — global prices and option names
                        // are intentionally visible before login so the builder can
                        // render the full option list during the selection flow.
                        .requestMatchers(HttpMethod.GET, "/api/package-options").permitAll()

                        // Legacy endpoints — kept open during the migration. These will
                        // either be removed or moved under proper role-based protection
                        // as the new domain model lands.
                        .requestMatchers("/api/event-types/**").permitAll()
                        .requestMatchers("/api/upgrades/**").permitAll()
                        .requestMatchers("/api/quotes/**").permitAll()
                        .requestMatchers("/api/config/**").permitAll()

                        // Anything else under /api requires auth
                        .requestMatchers("/api/**").authenticated()

                        // Default: allow (static resources, etc.)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
