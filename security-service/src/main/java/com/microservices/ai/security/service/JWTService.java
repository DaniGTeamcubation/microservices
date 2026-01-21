package com.microservices.ai.security.service;

import com.microservices.ai.common.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT Service for Security Service
 * Wraps JwtTokenUtil from common-library with Spring configuration
 */
@Service
@Slf4j
public class JWTService {

    private final JwtTokenUtil jwtTokenUtil;

    public JWTService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") Long expiration) {
        this.jwtTokenUtil = new JwtTokenUtil(secret, expiration);
        log.info("JwtService initialized with expiration: {}ms", expiration);
    }

    /**
     * Generate JWT token with username and role
     */
    public String generateToken(String username, String role) {
        return jwtTokenUtil.generateToken(username, role);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return jwtTokenUtil.extractUsername(token);
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        return jwtTokenUtil.extractRole(token);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return jwtTokenUtil.isTokenExpired(token);
    }

    /**
     * Get expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return jwtTokenUtil.getExpirationTime();
    }
}