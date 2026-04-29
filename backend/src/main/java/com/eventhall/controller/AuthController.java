package com.eventhall.controller;

import com.eventhall.dto.LoginRequest;
import com.eventhall.dto.LoginResponse;
import com.eventhall.dto.MeResponse;
import com.eventhall.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * - {@code POST /api/auth/login} is open to all (the only public endpoint).
 * - {@code GET  /api/auth/me} requires a valid JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and current-user endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email + password and receive a JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Return the currently-authenticated user")
    public MeResponse me() {
        return authService.currentUser();
    }
}
