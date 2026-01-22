package com.microservices.controller;

import com.microservices.dto.RecommendationRequest;
import com.microservices.dto.RecommendationResponse;
import com.microservices.service.PlanRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/ia/recommendations")
@Slf4j
public class RecommendationController {

    private final PlanRecommendationService recommendationService;

    public RecommendationController(PlanRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<RecommendationResponse> recommendPlan(
            @RequestBody RecommendationRequest request) {

        log.info("Received recommendation request for member: {}", request.getMemberId());

        RecommendationResponse response = recommendationService.recommendPlan(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}