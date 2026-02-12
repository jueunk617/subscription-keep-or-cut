package com.back.domain.usage.service;

import com.back.domain.category.entity.Category;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import com.back.domain.subscription.repository.SubscriptionRepository;
import com.back.domain.usage.dto.UsageRequest;
import com.back.domain.usage.entity.SubscriptionUsage;
import com.back.domain.usage.repository.SubscriptionUsageRepository;
import com.back.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @InjectMocks
    private UsageService usageService;

    @Mock
    private SubscriptionUsageRepository usageRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionEvaluationRepository evaluationRepository;

    @Test
    @DisplayName("사용량 및 평가 정상 생성")
    void t1() {
        // given
        int year = 2025;
        int month = 1;

        Category category = mock(Category.class);

        Subscription subscription = new Subscription(
                category,
                "Netflix",
                15000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        UsageRequest request = new UsageRequest(
                1L,
                year,
                month,
                100
        );

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));

        when(usageRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .thenReturn(Optional.empty());

        when(evaluationRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .thenReturn(Optional.empty());

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        verify(usageRepository, times(1)).save(any(SubscriptionUsage.class));
        verify(evaluationRepository, times(1)).save(any(SubscriptionEvaluation.class));
    }

    @Test
    @DisplayName("기존 사용량 존재 시 update 수행")
    void t2() {
        // given
        int year = 2025;
        int month = 1;

        Category category = mock(Category.class);

        Subscription subscription = new Subscription(
                category,
                "Netflix",
                15000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        SubscriptionUsage existingUsage =
                new SubscriptionUsage(subscription, year, month, 5);

        UsageRequest request = new UsageRequest(
                1L,
                year,
                month,
                20
        );

        when(subscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));

        when(usageRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .thenReturn(Optional.of(existingUsage));

        when(evaluationRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .thenReturn(Optional.empty());

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        assertEquals(20, existingUsage.getUsageValue());
        verify(usageRepository).save(existingUsage);
        verify(evaluationRepository).save(any(SubscriptionEvaluation.class));
    }

    @Test
    @DisplayName("예외 발생 - 구독이 존재하지 않는 경우")
    void t3() {
        // given
        UsageRequest request = new UsageRequest(
                99L,
                2025,
                1,
                100
        );

        when(subscriptionRepository.findById(99L))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class,
                () -> usageService.recordUsageAndEvaluate(request));

        verify(usageRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }
}
