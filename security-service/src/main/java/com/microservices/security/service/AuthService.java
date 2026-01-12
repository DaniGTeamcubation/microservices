package com.microservices.security.service;

import com.microservices.security.dto.LoginRequest;
import com.microservices.security.dto.LoginResponse;
import com.microservices.security.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    private final JWTService jwtService;

    // Hardcoded users for learning purposes
    private final List<User> users = Arrays.asList(
            User.ADMIN,
            User.USER
    );

    public AuthService(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Authenticate user and generate JWT token
     */
    public Optional<LoginResponse> login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());

        Optional<User> userOpt = users.stream()
                .filter(u -> u.getUsername().equals(loginRequest.getUsername())
                        && u.getPassword().equals(loginRequest.getPassword()))
                .findFirst();

        if (userOpt.isEmpty()) {
            log.warn("Login failed for user: {}", loginRequest.getUsername());
            return Optional.empty();
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .expiresIn(jwtService.getExpirationTime())
                .build();

        log.info("Login successful for user: {} with role: {}", user.getUsername(), user.getRole());
        return Optional.of(response);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}