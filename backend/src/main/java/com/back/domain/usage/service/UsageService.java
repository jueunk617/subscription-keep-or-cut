package com.back.domain.usage.service;

import com.back.domain.evaluation.entity.SubscriptionEvaluation;
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

        // 1️. 구독 조회
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2️. 사용량 Upsert
        SubscriptionUsage usage =
                usageRepository.findBySubscriptionAndYearAndMonth(
                                subscription,
                                request.year(),
                                request.month()
                        )
                        .map(existing -> {
                            existing.updateValue(request.usageValue());
                            return existing;
                        })
                        .orElseGet(() ->
                                new SubscriptionUsage(
                                        subscription,
                                        request.year(),
                                        request.month(),
                                        request.usageValue()
                                )
                        );

        usageRepository.save(usage);

        // 3️. 평가 Upsert
        SubscriptionEvaluation evaluation =
                evaluationRepository.findBySubscriptionAndYearAndMonth(
                                subscription,
                                request.year(),
                                request.month()
                        )
                        .orElseGet(() ->
                                new SubscriptionEvaluation(
                                        subscription,
                                        request.year(),
                                        request.month()
                                )
                        );

        evaluation.update(request.usageValue());

        evaluationRepository.save(evaluation);
    }
}
