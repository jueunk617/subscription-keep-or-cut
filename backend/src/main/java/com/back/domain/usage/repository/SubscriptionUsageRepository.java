package com.back.domain.usage.repository;

import com.back.domain.subscription.entity.Subscription;
import com.back.domain.usage.entity.SubscriptionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.Optional;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {

    // 특정 구독의 특정 연/월 사용량 데이터 찾기
    Optional<SubscriptionUsage> findBySubscriptionAndUsageMonth(Subscription subscription, YearMonth usageMonth);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SubscriptionUsage su where su.subscription.id = :subscriptionId")
    int deleteAllBySubscriptionId(@Param("subscriptionId") Long subscriptionId);

}