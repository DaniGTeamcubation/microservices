package com.microservices.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Common JWT utility class shared across microservices
 * Provides token generation, validation and claim extraction
 *
 * This class eliminates code duplication between:
 * - security-service (token generation + validation)
 * - api-gateway (token validation only)
 */
@Slf4j
public class JwtTokenUtil {

    private final String secret;
    private final Long expiration;

    /**
     * Constructor for services that only validate tokens (e.g., API Gateway)
     * @param secret JWT secret key (base64 encoded)
     */
    public JwtTokenUtil(String secret) {
        this.secret = secret;
        this.expiration = null;
    }

    /**
     * Constructor for services that generate and validate tokens (e.g., Security Service)
     * @param secret JWT secret key (base64 encoded)
     * @param expiration Token expiration time in milliseconds
     */
    public JwtTokenUtil(String secret, Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * Generate JWT token with username and role
     * @param username User's username
     * @param role User's role (e.g., ADMIN, USER)
     * @return JWT token string
     * @throws IllegalStateException if expiration is not configured
     */
    public String generateToken(String username, String role) {
        if (expiration == null) {
            throw new IllegalStateException("Cannot generate token without expiration configuration");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("Generated JWT token for user: {} with role: {}", username, role);
        return token;
    }

    /**
     * Validate JWT token signature and structure
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from token claims
     * @param token JWT token
     * @return username (subject)
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract role from token claims
     * @param token JWT token
     * @return role string
     */
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    /**
     * Check if token expiration date has passed
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get configured expiration time
     * @return expiration time in milliseconds
     */
    public Long getExpirationTime() {
        return expiration;
    }

    /**
     * Parse and extract all claims from JWT token
     * @param token JWT token
     * @return Claims object containing all token data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Convert base64 secret to cryptographic key
     * @return Key object for signing/validation
     */
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
