package com.microservice.util;

import com.microservices.ai.common.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT utility for API Gateway
 * Wraps JwtTokenUtil from common-library with Spring configuration
 *
 * This service only validates tokens (no generation capability)
 */
@Component
@Slf4j
public class JwtUtil {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.jwtTokenUtil = new JwtTokenUtil(secret);
        log.info("JwtUtil initialized for token validation only");
    }

    /**
     * Validate JWT token signature and structure
     */
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    /**
     * Extract username from token claims
     */
    public String extractUsername(String token) {
        return jwtTokenUtil.extractUsername(token);
    }

    /**
     * Extract role from token claims
     */
    public String extractRole(String token) {
        return jwtTokenUtil.extractRole(token);
    }

    /**
     * Check if token expiration date has passed
     */
    public boolean isTokenExpired(String token) {
        return jwtTokenUtil.isTokenExpired(token);
    }
}