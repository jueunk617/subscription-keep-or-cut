package com.back.domain.subscription.repository;

import com.back.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("select coalesce(sum(s.virtualMonthlyCost), 0) from Subscription s")
    int sumVirtualMonthlyCost();
}