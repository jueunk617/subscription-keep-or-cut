package com.back.domain.evaluation.repository;

import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface SubscriptionEvaluationRepository extends JpaRepository<SubscriptionEvaluation, Long> {

    // 특정 구독의 특정 연/월 평가 찾기 (단일 조회용)
    Optional<SubscriptionEvaluation> findBySubscriptionAndEvalMonth(Subscription subscription, YearMonth evalMonth);

    // 특정 연/월의 모든 평가 데이터 가져오기
    List<SubscriptionEvaluation> findAllByEvalMonth(YearMonth evalMonth);

    // 대시보드용: N+1 문제를 방지하기 위한 EntityGraph 적용 버전
    @EntityGraph(attributePaths = {"subscription", "subscription.category"})
    List<SubscriptionEvaluation> findAllWithSubscriptionAndCategoryByEvalMonth(YearMonth evalMonth);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SubscriptionEvaluation se where se.subscription.id = :subscriptionId")
    int deleteAllBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

}