package com.microservices.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private Long expiresIn; // milliseconds

    public LoginResponse(String token, String username, String role, Long expiresIn) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }
}