package com.back.domain.usage.service;

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
        // 1. 구독 정보 조회
        Subscription sub = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2. 사용량 저장 (Upsert: 존재하면 업데이트, 없으면 생성)
        SubscriptionUsage usage = usageRepository.findBySubscriptionAndYearAndMonth(sub, request.year(), request.month())
                .map(existing -> {
                    existing.updateValue(request.usageValue()); // 값을 실제로 바꿔줘야 함
                    return existing;
                })
                .orElseGet(() -> usageRepository.save(new SubscriptionUsage(sub, request.year(), request.month(), request.usageValue())));

        usageRepository.save(usage);

        // 3. 평가 로직 계산
        double efficiencyRate = (double) request.usageValue() / sub.getCategory().getReferenceValue() * 100;
        EvaluationStatus status = calculateStatus(efficiencyRate, request.usageValue());
        int annualWaste = calculateAnnualWaste(sub.getVirtualMonthlyCost(), efficiencyRate);

        // 4. 평가 결과 저장
        SubscriptionEvaluation evaluation = evaluationRepository.findBySubscriptionAndYearAndMonth(sub, request.year(), request.month())
                .map(e -> {
                    // 기존 데이터가 있으면 업데이트 로직 수행
                    return new SubscriptionEvaluation(sub, request.year(), request.month(), efficiencyRate, status, annualWaste);
                })
                .orElse(new SubscriptionEvaluation(sub, request.year(), request.month(), efficiencyRate, status, annualWaste));

        evaluationRepository.save(evaluation);
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