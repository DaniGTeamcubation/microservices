package com.microservices.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String recommendedPlan;

    @Column(length = 2000)
    private String reasoning;

    private Double confidenceScore;

    @Column(length = 1000)
    private String alternativePlans;

    @Column(length = 2000)
    private String considerationFactors;

    private LocalDateTime recommendedAt;

    @PrePersist
    protected void onCreate() {
        if (recommendedAt == null) {
            recommendedAt = LocalDateTime.now();
        }
    }
}
