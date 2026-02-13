package com.back.domain.dashboard.service;

import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SubscriptionEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getMonthlyDashboard(int year, int month) {

        /**
         * [정책]
         * 해당 월에 평가 (SubscriptionEvaluation)가 존재하는 구독만 대시보드에 포함한다.
         * 사용량 미입력 구독은 분석 대상에서 제외한다.
         */

        // 1. 해당 월의 모든 평가 데이터 조회
        List<SubscriptionEvaluation> evaluations = evaluationRepository.findAllWithSubscriptionAndCategoryByYearAndMonth(year, month);

        // 2. 이번 달 평가된 구독의 월 지출 합계
        long totalMonthlyCost = evaluations.stream()
                .mapToLong(e -> {
                    var s = e.getSubscription();
                    return s.getStatus() == SubscriptionStatus.TRIAL ? 0L : s.getMonthlyShareCost();
                })
                .sum();

        // 3. 전체 연간 낭비 예상액 합계
        long totalWaste = evaluations.stream()
                .mapToLong(SubscriptionEvaluation::getAnnualWaste)
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