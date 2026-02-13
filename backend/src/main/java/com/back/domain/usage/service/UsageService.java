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
import org.springframework.dao.DataIntegrityViolationException;
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
        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 2. 사용량 Upsert (동시성 대응)
        SubscriptionUsage usage = usageRepository.findBySubscriptionAndYearAndMonth(
                        subscription, request.year(), request.month()
                )
                .map(existing -> {
                    existing.updateValue(request.usageValue());
                    return existing;
                })
                .orElseGet(() -> new SubscriptionUsage(
                        subscription, request.year(), request.month(), request.usageValue()
                ));

        try {
            // flush로 유니크 위반을 즉시 감지
            usageRepository.saveAndFlush(usage);
        } catch (DataIntegrityViolationException e) {
            // 동시에 누군가가 먼저 INSERT 했을 가능성 -> 재조회 후 update로 마무리
            SubscriptionUsage existing = usageRepository.findBySubscriptionAndYearAndMonth(
                            subscription, request.year(), request.month()
                    )
                    .orElseThrow(() -> e);

            existing.updateValue(request.usageValue());
            usageRepository.save(existing);
        }

        // 3. 평가 Upsert (동시성 대응 추가)
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

        try {
            // flush로 유니크 위반을 즉시 감지
            evaluationRepository.saveAndFlush(evaluation);
        } catch (DataIntegrityViolationException e) {

            // 동시에 누군가가 먼저 INSERT 했을 가능성 -> 재조회 후 update로 마무리
            SubscriptionEvaluation existing = evaluationRepository.findBySubscriptionAndYearAndMonth(
                            subscription, request.year(), request.month()
                    )
                    .orElseThrow(() -> e);

            existing.update(request.usageValue());
            evaluationRepository.save(existing);
        }
    }
}
