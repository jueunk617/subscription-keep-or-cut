package com.back.domain.subscription.dto;

import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;

public record SubscriptionRequest(
        Long categoryId,
        String name,
        int totalCost,
        int userShareCost,
        BillingCycle billingCycle,
        SubscriptionStatus status
) {}