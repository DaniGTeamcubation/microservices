package com.microservices.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Integer memberAge;
    private String recommendedPlan;
    private String reasoning;
    private Double confidenceScore;
    private List<String> alternativePlans;
    private List<String> considerationFactors;
    private LocalDateTime recommendedAt;
}