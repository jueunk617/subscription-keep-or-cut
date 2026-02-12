package com.back.domain.usage.service;

import com.back.domain.category.enums.CategoryType;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.repository.SubscriptionRepository;
import com.back.domain.usage.dto.UsageRequest;
import com.back.domain.usage.entity.SubscriptionUsage;
import com.back.domain.usage.repository.SubscriptionUsageRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final SubscriptionUsageRepository usageRepository;
    private final SubscriptionEvaluationRepository evaluationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void recordUsageAndEvaluate(UsageRequest request) {

        // 1. 구독 조회
        Subscription sub = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2. 사용량 Upsert
        SubscriptionUsage usage = usageRepository
                .findBySubscriptionAndYearAndMonth(sub, request.year(), request.month())
                .map(existing -> {
                    existing.updateValue(request.usageValue());
                    return existing;
                })
                .orElseGet(() ->
                        new SubscriptionUsage(sub, request.year(), request.month(), request.usageValue())
                );

        usageRepository.save(usage);

        // 3. 효율 계산 (카테고리 타입 반영)
        double efficiencyRate = calculateEfficiency(sub, request.usageValue());

        // 4. 상태 및 낭비 금액 계산
        EvaluationStatus status = calculateStatus(efficiencyRate, request.usageValue());
        int annualWaste = calculateAnnualWaste(sub.getVirtualMonthlyCost(), efficiencyRate);

        // 5. 평가 Upsert
        SubscriptionEvaluation evaluation =
                evaluationRepository.findBySubscriptionAndYearAndMonth(sub, request.year(), request.month())
                        .map(existing -> {
                            existing.update(efficiencyRate, status, annualWaste);
                            return existing;
                        })
                        .orElseGet(() ->
                                new SubscriptionEvaluation(
                                        sub,
                                        request.year(),
                                        request.month(),
                                        efficiencyRate,
                                        status,
                                        annualWaste
                                )
                        );

        evaluationRepository.save(evaluation);
    }

    // 카테고리 타입 기반 효율 계산
    private double calculateEfficiency(Subscription sub, int usageValue) {

        int referenceValue = sub.getCategory().getReferenceValue();

        if (referenceValue == 0) {
            return 0;
        }

        double rate = (double) usageValue / referenceValue * 100;

        CategoryType type = sub.getCategory().getType();

        if (type == CategoryType.CONTENT) {
            // 콘텐츠형은 초과 사용도 그대로 반영
            return rate;
        }

        if (type == CategoryType.PRODUCTIVITY) {
            // 생산성형은 100% 이상은 의미 축소
            return Math.min(rate, 100);
        }

        return rate;
    }

    private EvaluationStatus calculateStatus(double rate, int value) {
        if (value == 0) return EvaluationStatus.GHOST;
        if (rate >= 100) return EvaluationStatus.EFFICIENT;
        if (rate >= 70) return EvaluationStatus.KEEP;
        if (rate >= 40) return EvaluationStatus.REVIEW;
        return EvaluationStatus.INEFFICIENT;
    }

    private int calculateAnnualWaste(int monthlyCost, double rate) {
        if (rate >= 100) return 0;
        return (int) (monthlyCost * (1 - rate / 100) * 12);
    }
}