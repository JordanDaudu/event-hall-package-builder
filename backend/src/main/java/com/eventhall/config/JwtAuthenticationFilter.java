package com.eventhall.config;

import com.eventhall.entity.UserAccount;
import com.eventhall.repository.UserAccountRepository;
import com.eventhall.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Reads the {@code Authorization: Bearer <token>} header on each request,
 * validates the JWT, and populates the Spring Security context.
 *
 * Skips itself silently if there is no token, the token is invalid, or the
 * context is already authenticated. Endpoints that require auth will then be
 * rejected by the security filter chain with a 401/403.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserAccountRepository userAccountRepository
    ) {
        this.jwtService = jwtService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseToken(token);
            String email = claims.get(JwtService.CLAIM_EMAIL, String.class);
            String tokenRole = claims.get(JwtService.CLAIM_ROLE, String.class);

            if (email != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Re-validate against the database on every authenticated request.
                // This ensures that disabled accounts and role changes take effect
                // immediately (without waiting for the JWT to expire).
                Optional<UserAccount> maybeUser = userAccountRepository.findByEmailIgnoreCase(email);
                if (maybeUser.isEmpty() || !maybeUser.get().isActive()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String dbRole = maybeUser.get().getRole().name();
                // If the role in the token no longer matches the DB, trust the DB
                // (most secure: prevents privilege escalation if a token is replayed
                // after a role downgrade).
                String effectiveRole = (tokenRole != null && tokenRole.equals(dbRole)) ? tokenRole : dbRole;

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole));
                var principal = User.builder()
                        .username(email)
                        .password("")
                        .authorities(authorities)
                        .build();

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException ex) {
            // Invalid / expired token — leave context unauthenticated and let
            // downstream security rules return 401/403 as appropriate.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
