package com.back.domain.evaluation.repository;

import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionEvaluationRepository extends JpaRepository<SubscriptionEvaluation, Long> {

    // 특정 구독의 특정 연/월 평가 찾기 (단일 조회용)
    Optional<SubscriptionEvaluation> findBySubscriptionAndYearAndMonth(Subscription subscription, int year, int month);

    // 특정 연/월의 모든 평가 데이터 가져오기
    List<SubscriptionEvaluation> findAllByYearAndMonth(int year, int month);
}