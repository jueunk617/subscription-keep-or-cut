package com.back.domain.dashboard.service;

import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SubscriptionEvaluationRepository evaluationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getMonthlyDashboard(int year, int month) {
        // 1. 해당 월의 모든 평가 데이터 조회
        var evaluations = evaluationRepository.findAllWithSubscriptionAndCategoryByYearAndMonth(year, month);

        // 2. 전체 월 지출액 합계 (등록된 모든 구독 기준)
        int totalMonthlyCost = subscriptionRepository.sumVirtualMonthlyCost();

        // 3. 전체 연간 낭비 예상액 합계
        int totalWaste = evaluations.stream()
                .mapToInt(e -> e.getAnnualWaste())
                .sum();

        // 4. 구독별 요약 리스트 생성
        List<DashboardResponse.SubscriptionSummary> summaries = evaluations.stream()
                .map(e -> new DashboardResponse.SubscriptionSummary(
                        e.getSubscription().getId(),
                        e.getSubscription().getCategory().getName(),
                        e.getSubscription().getName(),
                        e.getEfficiencyRate(),
                        e.getStatus(),
                        e.getAnnualWaste()
                )).toList();

        return new DashboardResponse(totalMonthlyCost, totalWaste, summaries);
    }
}