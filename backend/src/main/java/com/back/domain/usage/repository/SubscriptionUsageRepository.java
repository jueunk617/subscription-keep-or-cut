package com.back.domain.usage.repository;

import com.back.domain.usage.entity.SubscriptionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {
}