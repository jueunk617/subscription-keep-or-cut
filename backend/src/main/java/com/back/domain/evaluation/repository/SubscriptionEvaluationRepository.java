package com.back.domain.evaluation.repository;

import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionEvaluationRepository extends JpaRepository<SubscriptionEvaluation, Long> {

    // 특정 구독의 특정 연/월 평가 찾기 (Upsert용)
    Optional<SubscriptionEvaluation> findBySubscriptionAndYearAndMonth(Subscription subscription, int year, int month);
}