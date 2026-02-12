package com.back.domain.evaluation.repository;

import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionEvaluationRepository extends JpaRepository<SubscriptionEvaluation, Long> {
}