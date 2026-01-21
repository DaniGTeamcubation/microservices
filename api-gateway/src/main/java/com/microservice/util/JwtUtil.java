package com.microservice.util;

import com.microservices.common.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.jwtTokenUtil = new JwtTokenUtil(secret);
        log.info("JwtUtil initialized for token validation only");
    }

    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }

    public String extractUsername(String token) {
        return jwtTokenUtil.extractUsername(token);
    }

    public String extractRole(String token) {
        return jwtTokenUtil.extractRole(token);
    }

    public boolean isTokenExpired(String token) {
        return jwtTokenUtil.isTokenExpired(token);
    }
}