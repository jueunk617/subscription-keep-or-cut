package com.back.domain.usage.service;

import com.back.domain.category.enums.UsageUnit;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.policy.EvaluationPolicy;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.repository.SubscriptionRepository;
import com.back.domain.usage.dto.UsageRequest;
import com.back.domain.usage.entity.SubscriptionUsage;
import com.back.domain.usage.repository.SubscriptionUsageRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class UsageService {

    private final SubscriptionUsageRepository usageRepository;
    private final SubscriptionEvaluationRepository evaluationRepository;
    private final SubscriptionRepository subscriptionRepository;

    // 기본 정책 주입 (나중에 카테고리별 정책 매니저를 두어 동적으로 바꿀 수도 있음)
    private final EvaluationPolicy evaluationPolicy;

    @Transactional
    public void recordUsageAndEvaluate(UsageRequest request) {

        // 1. 구독 조회
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 1-1. UsageUnit 기반 사용량 검증
        validateUsageValue(
                subscription.getCategory().getUnit(),
                request.date(),
                request.usageValue()
        );

        // 2. 사용량 Upsert (동시성 대응)
        SubscriptionUsage usage = usageRepository.findBySubscriptionAndUsageMonth(subscription, request.date())
                .map(existing -> {
                    existing.updateValue(request.usageValue());
                    return existing;
                })
                .orElseGet(() -> new SubscriptionUsage(subscription, request.date(), request.usageValue()));

        try {
            usageRepository.saveAndFlush(usage);
        } catch (DataIntegrityViolationException e) {
            // [수정] 재조회 시에도 변경된 메서드 명 적용
            SubscriptionUsage existing = usageRepository.findBySubscriptionAndUsageMonth(subscription, request.date())
                    .orElseThrow(() -> e);

            existing.updateValue(request.usageValue());
            usageRepository.save(existing);
        }

        // 3. 평가 Upsert (동시성 대응 추가)
        SubscriptionEvaluation evaluation =
                evaluationRepository.findBySubscriptionAndEvalMonth(subscription, request.date())
                        .orElseGet(() -> new SubscriptionEvaluation(subscription, request.date()));

        evaluation.update(request.usageValue(), evaluationPolicy);

        try {
            evaluationRepository.saveAndFlush(evaluation);
        } catch (DataIntegrityViolationException e) {
            SubscriptionEvaluation existing = evaluationRepository.findBySubscriptionAndEvalMonth(subscription, request.date())
                    .orElseThrow(() -> e);

            existing.update(request.usageValue(), evaluationPolicy);
            evaluationRepository.save(existing);
        }
    }

    private void validateUsageValue(UsageUnit unit, YearMonth date, int usageValue) {

        if (usageValue < 0) {
            throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
        }

        int lengthOfMonth = date.lengthOfMonth();

        if (unit == UsageUnit.DAYS) {
            if (usageValue > lengthOfMonth) {
                throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
            }
            return;
        }

        if (unit == UsageUnit.MINUTES) {
            int maxMinutes = lengthOfMonth * 24 * 60;
            if (usageValue > maxMinutes) {
                throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
            }
        }
    }
}