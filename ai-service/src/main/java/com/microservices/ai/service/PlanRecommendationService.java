package com.microservices.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.ai.client.MemberClient;
import com.microservices.ai.dto.RecommendationRequest;
import com.microservices.ai.dto.RecommendationResponse;
import com.microservices.ai.entity.PlanRecommendation;
import com.microservices.ai.repository.PlanRecommendationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for AI-powered insurance plan recommendations
 */
@Service
@Slf4j
public class PlanRecommendationService {

    private final PlanRecommendationRepository recommendationRepository;
    private final MemberClient memberClient;
    private final OpenRouterService openRouterService;
    private final ObjectMapper objectMapper;

    private static final String RECOMMENDATION_SYSTEM_PROMPT = """
            You are an insurance plan recommendation AI assistant.
            Your task is to analyze member profiles and recommend the most suitable insurance plan.
            
            Available insurance plans:
            - BASIC: Low premium, basic coverage, suitable for young healthy individuals
            - STANDARD: Moderate premium, good coverage, suitable for average needs
            - PREMIUM: High premium, comprehensive coverage, suitable for families or high needs
            - SENIOR: Specialized coverage for elderly, includes extended care
            - FAMILY: Optimized for families with children, includes pediatric care
            
            Consider these factors:
            - Member age and health status
            - Medical history and chronic conditions
            - Family situation
            - Budget considerations
            - Specific preferences mentioned
            
            Respond ONLY with a JSON object in this exact format:
            {
              "recommendedPlan": "PLAN_NAME",
              "reasoning": "detailed explanation of why this plan is recommended",
              "confidenceScore": 0.90,
              "alternativePlans": ["PLAN1", "PLAN2"],
              "considerationFactors": ["factor1", "factor2", "factor3"]
            }
            
            Do not include any other text, explanations, or markdown formatting.
            """;

    public PlanRecommendationService(
            PlanRecommendationRepository recommendationRepository,
            MemberClient memberClient,
            OpenRouterService openRouterService) {
        this.recommendationRepository = recommendationRepository;
        this.memberClient = memberClient;
        this.openRouterService = openRouterService;
        this.objectMapper = new ObjectMapper();
    }

    public RecommendationResponse recommendPlan(RecommendationRequest request) {
        log.info("Generating plan recommendation for member: {}", request.getMemberId());

        Map<String, Object> memberData = memberClient.getMemberById(request.getMemberId());

        String userMessage = buildRecommendationPrompt(memberData, request);

        try {
            String aiResponse = openRouterService.chatCompletion(
                    RECOMMENDATION_SYSTEM_PROMPT,
                    userMessage
            );

            PlanRecommendation recommendation = createRecommendationFromAiResponse(
                    request.getMemberId(),
                    aiResponse
            );

            recommendation = recommendationRepository.save(recommendation);

            log.info("Recommendation created successfully for member: {}", request.getMemberId());

            return mapToResponse(recommendation, memberData);

        } catch (Exception e) {
            log.error("Error generating recommendation for member: {}", request.getMemberId(), e);
            throw new RuntimeException("Failed to generate plan recommendation", e);
        }
    }

    public List<RecommendationResponse> getRecommendationHistory(Long memberId) {
        Map<String, Object> memberData = memberClient.getMemberById(memberId);

        return recommendationRepository.findByMemberId(memberId).stream()
                .map(rec -> mapToResponse(rec, memberData))
                .toList();
    }

    private String buildRecommendationPrompt(
            Map<String, Object> memberData,
            RecommendationRequest request) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Please recommend an insurance plan for this member:\n\n");

        prompt.append("Member Profile:\n");
        prompt.append("- Name: ").append(memberData.get("name")).append("\n");
        prompt.append("- Age: ").append(memberData.get("age")).append("\n");
        prompt.append("- Email: ").append(memberData.get("email")).append("\n\n");

        if (request.getMedicalHistory() != null && !request.getMedicalHistory().isEmpty()) {
            prompt.append("Medical History:\n");
            prompt.append(request.getMedicalHistory()).append("\n\n");
        }

        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            prompt.append("Preferences:\n");
            request.getPreferences().forEach(pref ->
                    prompt.append("- ").append(pref).append("\n"));
            prompt.append("\n");
        }

        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().isEmpty()) {
            prompt.append("Additional Information:\n");
            prompt.append(request.getAdditionalInfo()).append("\n\n");
        }

        prompt.append("Based on this information, what insurance plan would you recommend?");

        return prompt.toString();
    }

    private PlanRecommendation createRecommendationFromAiResponse(
            Long memberId,
            String aiResponse) {

        try {
            aiResponse = aiResponse.trim();
            if (aiResponse.startsWith("```json")) {
                aiResponse = aiResponse.substring(7);
            }
            if (aiResponse.endsWith("```")) {
                aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
            }
            aiResponse = aiResponse.trim();

            Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);

            PlanRecommendation recommendation = new PlanRecommendation();
            recommendation.setMemberId(memberId);
            recommendation.setRecommendedPlan((String) result.get("recommendedPlan"));
            recommendation.setReasoning((String) result.get("reasoning"));
            recommendation.setConfidenceScore(((Number) result.get("confidenceScore")).doubleValue());

            if (result.get("alternativePlans") != null) {
                recommendation.setAlternativePlans(String.join(", ",
                        (List<String>) result.get("alternativePlans")));
            }

            if (result.get("considerationFactors") != null) {
                recommendation.setConsiderationFactors(String.join(", ",
                        (List<String>) result.get("considerationFactors")));
            }

            recommendation.setRecommendedAt(LocalDateTime.now());

            return recommendation;

        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            throw new RuntimeException("Failed to parse AI recommendation response", e);
        }
    }

    private RecommendationResponse mapToResponse(
            PlanRecommendation recommendation,
            Map<String, Object> memberData) {

        List<String> alternativePlans = recommendation.getAlternativePlans() != null ?
                Arrays.asList(recommendation.getAlternativePlans().split(", ")) :
                List.of();

        List<String> factors = recommendation.getConsiderationFactors() != null ?
                Arrays.asList(recommendation.getConsiderationFactors().split(", ")) :
                List.of();

        return RecommendationResponse.builder()
                .id(recommendation.getId())
                .memberId(recommendation.getMemberId())
                .memberName((String) memberData.get("name"))
                .memberAge((Integer) memberData.get("age"))
                .recommendedPlan(recommendation.getRecommendedPlan())
                .reasoning(recommendation.getReasoning())
                .confidenceScore(recommendation.getConfidenceScore())
                .alternativePlans(alternativePlans)
                .considerationFactors(factors)
                .recommendedAt(recommendation.getRecommendedAt())
                .build();
    }
}
