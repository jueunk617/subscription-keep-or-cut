package com.back.domain.subscription.dto;

import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(

        @NotNull(message = "카테고리는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "구독 이름은 필수입니다.")
        String name,

        @Min(value = 0, message = "총 비용은 0 이상이어야 합니다.")
        long totalCost,

        @Min(value = 0, message = "사용자 부담 금액은 0 이상이어야 합니다.")
        long userShareCost,

        @NotNull(message = "결제 주기는 필수입니다.")
        BillingCycle billingCycle,

        @NotNull(message = "구독 상태는 필수입니다.")
        SubscriptionStatus status
) {}
