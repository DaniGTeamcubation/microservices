package com.microservice.filter;

import com.microservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/health",
            "/actuator"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        log.info("Processing request: {} {}", request.getMethod(), path);

        if (isPublicPath(path)) {
            log.info("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        if (jwtUtil.isTokenExpired(token)) {
            log.warn("Expired JWT token for path: {}", path);
            return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        log.info("Authenticated user: {} with role: {} accessing: {}", username, role, path);

        if (!hasAccess(request.getMethod().toString(), role)) {
            log.warn("User {} with role {} has no access to: {} {}", username, role, request.getMethod(), path);
            return onError(exchange, "Access denied: Insufficient permissions", HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Username", username)
                .header("X-User-Role", role)
                .build();

        log.info("Request authorized. Forwarding to downstream service with user context");

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean hasAccess(String method, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }

        if ("USER".equals(role)) {
            return "GET".equals(method);
        }

        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}",
                message, status.value());

        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(errorResponse.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // High priority - execute before other filters
    }
}