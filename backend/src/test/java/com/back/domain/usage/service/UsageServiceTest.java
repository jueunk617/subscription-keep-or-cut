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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
                15000L, // totalCost
                15000L, // userShareCost
                15000L, // monthlyShareCost
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        UsageRequest request = new UsageRequest(1L, year, month, 100);

        given(subscriptionRepository.findById(1L))
                .willReturn(Optional.of(subscription));

        given(usageRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.empty());

        given(evaluationRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.empty());

        given(usageRepository.saveAndFlush(any(SubscriptionUsage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(evaluationRepository.saveAndFlush(any(SubscriptionEvaluation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        verify(usageRepository, times(1)).saveAndFlush(any(SubscriptionUsage.class));
        verify(evaluationRepository, times(1)).saveAndFlush(any(SubscriptionEvaluation.class));
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
                15000L,
                15000L,
                15000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        SubscriptionUsage existingUsage = new SubscriptionUsage(subscription, year, month, 5);

        UsageRequest request = new UsageRequest(1L, year, month, 20);

        given(subscriptionRepository.findById(1L))
                .willReturn(Optional.of(subscription));

        given(usageRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.of(existingUsage));

        given(evaluationRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.empty());

        given(usageRepository.saveAndFlush(any(SubscriptionUsage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(evaluationRepository.saveAndFlush(any(SubscriptionEvaluation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        assertEquals(20, existingUsage.getUsageValue());
        verify(usageRepository).saveAndFlush(existingUsage);
        verify(evaluationRepository).saveAndFlush(any(SubscriptionEvaluation.class));
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

        given(subscriptionRepository.findById(99L))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class,
                () -> usageService.recordUsageAndEvaluate(request));

        verify(usageRepository, never()).saveAndFlush(any());
        verify(evaluationRepository, never()).saveAndFlush(any());
        verify(usageRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }
}
