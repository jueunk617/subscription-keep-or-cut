package com.back.domain.dashboard.dto;

import com.back.domain.evaluation.enums.EvaluationStatus;
import java.util.List;

public record DashboardResponse(
        long totalMonthlyCost,          // 해당 월 "평가된 구독" 기준 월 환산 사용자 부담금 합계
        long totalAnnualWasteEstimate,  // 해당 월 평가된 구독 기준 연간 낭비 추정 합계
        List<SubscriptionSummary> subscriptions
) {
    public record SubscriptionSummary(
            Long id,
            String categoryName,
            String name,
            double efficiencyRate,
            EvaluationStatus status,
            long annualWaste,
            boolean trial,
            long potentialAnnualWaste
    ) {}
}