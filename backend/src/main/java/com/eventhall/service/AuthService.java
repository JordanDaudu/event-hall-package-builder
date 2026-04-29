package com.eventhall.service;

import com.eventhall.dto.LoginRequest;
import com.eventhall.dto.LoginResponse;
import com.eventhall.dto.MeResponse;
import com.eventhall.entity.UserAccount;
import com.eventhall.repository.UserAccountRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * AuthService handles login and the "who am I" lookup.
 *
 * Login flow:
 *  1. Delegate credential check to Spring Security's {@link AuthenticationManager}.
 *  2. On success, load the {@link UserAccount} from the database.
 *  3. Issue a JWT via {@link JwtService}.
 *  4. Return a clean response (never the password hash).
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (DisabledException ex) {
            throw new ResponseStatusException(FORBIDDEN, "User account is disabled");
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication failed");
        }

        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication failed"));

        String token = jwtService.issueToken(user);
        return new LoginResponse(
                token,
                jwtService.getExpirationMinutes(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public MeResponse currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Not authenticated");
        }
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
        return new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCustomerIdentityNumber(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isActive(),
                user.getBasePackagePrice()
        );
    }
}
