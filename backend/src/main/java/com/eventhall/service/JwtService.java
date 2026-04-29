package com.eventhall.service;

import com.eventhall.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * JwtService is responsible for issuing and parsing JSON Web Tokens.
 *
 * Tokens carry:
 * - subject  = user account id (as string)
 * - email    = user email
 * - role     = ADMIN | CUSTOMER
 *
 * The signing secret is loaded from the {@code app.security.jwt.secret} property
 * (env var {@code JWT_SECRET}). It must be at least 256 bits (32 bytes) for HS256.
 */
@Service
public class JwtService {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMinutes;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes,
            @Value("${app.security.jwt.issuer}") String issuer
    ) {
        // We treat the configured secret as raw UTF-8 bytes. This keeps
        // configuration simple — operators can use any sufficiently long
        // random string (>= 32 bytes / 256 bits) as the JWT_SECRET env var.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes (256 bits) for HS256. Got " + keyBytes.length + " bytes."
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
    }

    /**
     * Issue a signed JWT for the given user account.
     */
    public String issueToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .claims(Map.of(
                        CLAIM_EMAIL, user.getEmail(),
                        CLAIM_ROLE, user.getRole().name()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parse and validate a token. Throws {@link JwtException} on any failure
     * (signature mismatch, expiration, malformed token, etc.).
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
