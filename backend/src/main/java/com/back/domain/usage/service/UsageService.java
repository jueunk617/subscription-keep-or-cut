package com.back.domain.usage.service;

import com.back.domain.category.enums.UsageUnit;
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

import java.time.YearMonth;

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

        // 1-1. UsageUnit 기반 사용량 검증 (월 입력 정책: 해당 월의 최대 일수/분으로 상한 결정)
        validateUsageValue(
                subscription.getCategory().getUnit(),
                request.year(),
                request.month(),
                request.usageValue()
        );

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

    /**
     * UsageUnit에 따라 허용 가능한 usageValue 범위 검증 (월 단위 입력 정책)
     * - DAYS: 0 ~ 해당 월의 최대 일수 (e.g. 2월 28/29, 7월 31)
     * - MINUTES: 0 ~ (해당 월 최대 일수 * 24 * 60)
     */
    private void validateUsageValue(UsageUnit unit, int year, int month, int usageValue) {

        if (usageValue < 0) {
            throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
        }

        YearMonth yearMonth = YearMonth.of(year, month);

        if (unit == UsageUnit.DAYS) {
            int maxDays = yearMonth.lengthOfMonth();
            if (usageValue > maxDays) {
                throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
            }
            return;
        }

        if (unit == UsageUnit.MINUTES) {
            int maxMinutes = yearMonth.lengthOfMonth() * 24 * 60;
            if (usageValue > maxMinutes) {
                throw new CustomException(ErrorCode.INVALID_USAGE_VALUE);
            }
        }
    }
}
