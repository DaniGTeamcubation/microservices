package com.microservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${app.message:No message configured}")
    private String message;

    @Value("${server.port}")
    private String port;

    @GetMapping
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("message", message);
        config.put("port", port);
        config.put("service", "claim-service");
        return config;
    }
}