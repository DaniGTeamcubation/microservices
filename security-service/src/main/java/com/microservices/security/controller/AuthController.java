package com.microservices.security.controller;

import com.microservices.security.dto.LoginRequest;
import com.microservices.security.dto.LoginResponse;
import com.microservices.security.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login endpoint - returns JWT token
     * POST /auth/login
     * Body: { "username": "admin", "password": "admin" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());

        Optional<LoginResponse> response = authService.login(loginRequest);

        if (response.isEmpty()) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        log.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response.get());
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Security Service is running");
    }
}