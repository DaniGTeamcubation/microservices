package com.microservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @GetMapping("/trace")
    public Map<String, String> testTrace() {
        log.info("Member Service - Processing trace test");

        Map<String, String> response = new HashMap<>();
        response.put("service", "member-service");
        response.put("message", "Trace test from member service");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return response;
    }
}
