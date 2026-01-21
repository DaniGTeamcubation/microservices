package com.microservices.ai.repository;

import com.microservices.ai.entity.PlanRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRecommendationRepository extends JpaRepository<PlanRecommendation, Long> {

    List<PlanRecommendation> findByMemberId(Long memberId);
}
