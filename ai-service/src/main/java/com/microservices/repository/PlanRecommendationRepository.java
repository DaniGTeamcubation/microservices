package com.microservices.repository;

import com.microservices.entity.PlanRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRecommendationRepository extends JpaRepository<com.microservices.entity.PlanRecommendation, Long> {

    List<PlanRecommendation> findByMemberId(Long memberId);
}
