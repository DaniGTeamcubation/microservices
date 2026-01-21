package com.microservices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.client.MemberClient;
import com.microservices.dto.RecommendationRequest;
import com.microservices.dto.RecommendationResponse;
import com.microservices.entity.PlanRecommendation;
import com.microservices.provider.AIProvider;
import com.microservices.repository.PlanRecommendationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PlanRecommendationService {

    private final PlanRecommendationRepository repository;
    private final MemberClient memberClient;
    private final AIProvider aiProvider;
    private final ObjectMapper objectMapper;

    private static final String RECOMMENDATION_SYSTEM_PROMPT = """
            You are an insurance plan recommendation AI assistant.

            Available plans:
            BASIC, STANDARD, PREMIUM, SENIOR, FAMILY

            Respond ONLY with JSON:
            {
              "recommendedPlan": "PLAN",
              "reasoning": "explanation",
              "confidenceScore": 0.90,
              "alternativePlans": ["PLAN1","PLAN2"],
              "considerationFactors": ["factor1","factor2"]
            }
            """;

    public PlanRecommendationService(
            PlanRecommendationRepository repository,
            MemberClient memberClient,
            AIProvider aiProvider,
            ObjectMapper objectMapper) {

        this.repository = repository;
        this.memberClient = memberClient;
        this.aiProvider = aiProvider;
        this.objectMapper = objectMapper;
    }

    public RecommendationResponse recommendPlan(RecommendationRequest request) {

        Map<String, Object> memberData =
                memberClient.getMemberById(request.getMemberId());

        String userPrompt = buildUserPrompt(memberData, request);

        try {
            String aiResponse = aiProvider.generateWithContext(
                    RECOMMENDATION_SYSTEM_PROMPT,
                    userPrompt
            );

            PlanRecommendation recommendation =
                    parseAiResponse(request.getMemberId(), aiResponse);

            repository.save(recommendation);
            return mapToResponse(recommendation, memberData);

        } catch (Exception e) {
            log.error("Plan recommendation failed", e);
            throw new IllegalStateException("AI recommendation failed", e);
        }
    }

    private PlanRecommendation parseAiResponse(Long memberId, String response)
            throws Exception {

        String cleanJson = response
                .replace("```json", "")
                .replace("```", "")
                .trim();

        Map<String, Object> result =
                objectMapper.readValue(cleanJson, Map.class);

        PlanRecommendation r = new PlanRecommendation();
        r.setMemberId(memberId);
        r.setRecommendedPlan((String) result.get("recommendedPlan"));
        r.setReasoning((String) result.get("reasoning"));
        r.setConfidenceScore(((Number) result.get("confidenceScore")).doubleValue());
        r.setAlternativePlans(String.join(", ",
                (List<String>) result.get("alternativePlans")));
        r.setConsiderationFactors(String.join(", ",
                (List<String>) result.get("considerationFactors")));
        r.setRecommendedAt(LocalDateTime.now());

        return r;
    }

    private String buildUserPrompt(
            Map<String, Object> memberData,
            RecommendationRequest request) {

        return """
                Member profile:
                Name: %s
                Age: %s
                Medical history: %s
                Preferences: %s
                Additional info: %s
                """
                .formatted(
                        memberData.get("name"),
                        memberData.get("age"),
                        request.getMedicalHistory(),
                        request.getPreferences(),
                        request.getAdditionalInfo()
                );
    }

    private RecommendationResponse mapToResponse(
            PlanRecommendation r,
            Map<String, Object> memberData) {

        return RecommendationResponse.builder()
                .id(r.getId())
                .memberId(r.getMemberId())
                .memberName((String) memberData.get("name"))
                .memberAge((Integer) memberData.get("age"))
                .recommendedPlan(r.getRecommendedPlan())
                .reasoning(r.getReasoning())
                .confidenceScore(r.getConfidenceScore())
                .alternativePlans(List.of(r.getAlternativePlans().split(", ")))
                .considerationFactors(List.of(r.getConsiderationFactors().split(", ")))
                .recommendedAt(r.getRecommendedAt())
                .build();
    }
}
