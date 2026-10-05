package com.cogito.authminijira.controller;

import com.cogito.authminijira.service.AuthService;
import com.cogito.minijira.common.dto.LoginRequest;
import com.cogito.minijira.common.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - REST endpoints for user authentication
 * 
 * Provides endpoints for user registration, login, token refresh, and user existence checks.
 * These endpoints handle all authentication-related operations.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    // ========== Logger ==========

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // ========== Dependencies ==========

    private final AuthService authService;

    /**
     * Constructs an AuthController with required dependencies
     * 
     * @param authService the authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ========== REST Endpoints ==========

    /**
     * Registers a new user account
     * 
     * @param request the registration request containing email, username, and password
     * @return success message if registration succeeds, error message otherwise
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns JWT tokens
     * 
     * @param request the login request containing email and password
     * @return AuthResponse with access and refresh tokens, or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Refreshes an expired access token using a valid refresh token
     * 
     * @param refreshToken the refresh token
     * @return new access token, or error message
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refreshToken(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh failed: " + e.getMessage());
        }
    }

    /**
     * Checks if a user exists by ID
     * 
     * @param id the user ID to check
     * @return true if the user exists, false otherwise
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> exists(@PathVariable Long id) {
        return ResponseEntity.ok(authService.exists(id));
    }
}
