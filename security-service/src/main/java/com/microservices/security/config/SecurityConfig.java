package com.microservices.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure security filter chain
     * Allow all requests to /auth/** (login endpoint)
     * Use stateless session management
     *
     * CSRF Protection is disabled because:
     * - This service uses JWT tokens (stateless authentication)
     * - No session cookies are used
     * - All requests require explicit Authorization header
     * - CSRF attacks target session-based authentication
     * This is a standard practice for JWT-based REST APIs
     */
    @Bean
    @SuppressWarnings("squid:S4502") // CSRF protection disabled intentionally for JWT stateless API
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF not needed for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}