package com.back.domain.usage.repository;

import com.back.domain.subscription.entity.Subscription;
import com.back.domain.usage.entity.SubscriptionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {

    // 특정 구독의 특정 연/월 사용량 데이터 찾기
    Optional<SubscriptionUsage> findBySubscriptionAndUsageMonth(Subscription subscription, YearMonth usageMonth);
}