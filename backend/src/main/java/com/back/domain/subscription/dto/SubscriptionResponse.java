package com.back.domain.subscription.dto;

import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;

public record SubscriptionResponse(
        Long id,
        String categoryName,
        String name,
        int virtualMonthlyCost,
        BillingCycle billingCycle,
        SubscriptionStatus status
) {}