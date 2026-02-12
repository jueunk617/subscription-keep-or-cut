package com.back.domain.dashboard.dto;

import com.back.domain.evaluation.enums.EvaluationStatus;
import java.util.List;

public record DashboardResponse(
        int totalMonthlyCost,          // 이번 달 총 구독 지출액
        int totalAnnualWasteEstimate,  // 이번 달 기준 연간 예상 낭비액 합계
        List<SubscriptionSummary> subscriptions
) {
    public record SubscriptionSummary(
            Long id,
            String categoryName,
            String name,
            double efficiencyRate,
            EvaluationStatus status,
            int annualWaste
    ) {}
}