package com.microservices.ai.controller;

import com.microservices.ai.dto.RecommendationRequest;
import com.microservices.ai.dto.RecommendationResponse;
import com.microservices.ai.service.PlanRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RecommendationResponse>> getRecommendationHistory(
            @PathVariable Long memberId) {

        log.info("Retrieving recommendation history for member: {}", memberId);

        List<RecommendationResponse> history =
                recommendationService.getRecommendationHistory(memberId);

        return ResponseEntity.ok(history);
    }
}