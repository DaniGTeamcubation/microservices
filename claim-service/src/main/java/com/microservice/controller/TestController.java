package com.microservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @GetMapping("/trace")
    public Map<String, Object> testTrace() {
        log.info("Claim Service - Processing trace test");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "claim-service");
        response.put("message", "Trace test from claim service");

        try {
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }

            String memberServiceUrl = "http://localhost:8081/api/test/trace";
            Map<String, Object> memberResponse = restTemplate.getForObject(memberServiceUrl, Map.class);
            response.put("called_member_service", memberResponse);
            log.info("Successfully called member-service");
        } catch (Exception e) {
            log.error("Error calling member-service: {}", e.getMessage());
            response.put("error", "Could not call member-service: " + e.getMessage());
        }

        return response;
    }
}