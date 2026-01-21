package com.microservices.ai.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private String role;

    // Hardcoded users for learning purposes
    public static final User ADMIN = new User("admin", "admin", "ADMIN");
    public static final User USER = new User("user", "user", "USER");
}