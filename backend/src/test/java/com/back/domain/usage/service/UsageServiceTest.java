package com.back.domain.usage.service;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.*;

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
        given(category.getUnit()).willReturn(UsageUnit.MINUTES);

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
        given(category.getUnit()).willReturn(UsageUnit.MINUTES);

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

    @Test
    @DisplayName("예외 - 단위가 DAYS인 구독에서 usageValue가 해당 월 일수를 초과하면 예외 발생")
    void t4() {
        int year = 2025;
        int month = 2; // 2025년 2월 = 28일

        Category category = new Category("AI_TOOL", 15, UsageUnit.DAYS, CategoryType.PRODUCTIVITY);

        Subscription subscription = new Subscription(
                category,
                "ChatGPT Plus",
                29000L, 29000L, 29000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        UsageRequest request = new UsageRequest(1L, year, month, 29); // 28 초과

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        assertThrows(CustomException.class, () -> usageService.recordUsageAndEvaluate(request));

        verify(usageRepository, never()).saveAndFlush(any());
        verify(evaluationRepository, never()).saveAndFlush(any());
        verify(usageRepository, never()).save(any());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    @DisplayName("동시성 상황 - 사용량 저장 중 유니크 충돌 발생 시 재조회 후 update 처리")
    void t5() {
        // given
        int year = 2025;
        int month = 3;

        Category category = mock(Category.class);
        given(category.getUnit()).willReturn(UsageUnit.MINUTES);

        Subscription subscription = new Subscription(
                category,
                "Netflix",
                15000L,
                15000L,
                15000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        UsageRequest request = new UsageRequest(1L, year, month, 100);

        given(subscriptionRepository.findById(1L))
                .willReturn(Optional.of(subscription));

        // 최초 조회: empty -> insert 시도
        // catch 재조회: existing 반환
        SubscriptionUsage existing = new SubscriptionUsage(subscription, year, month, 5);

        given(usageRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(existing));

        given(usageRepository.saveAndFlush(any(SubscriptionUsage.class)))
                .willThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key"));

        // evaluation은 그냥 정상 흐름만
        given(evaluationRepository.findBySubscriptionAndYearAndMonth(subscription, year, month))
                .willReturn(Optional.empty());

        given(evaluationRepository.saveAndFlush(any(SubscriptionEvaluation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        verify(usageRepository, times(1)).saveAndFlush(any(SubscriptionUsage.class)); // try에서 1회
        verify(usageRepository, times(1)).save(existing); // catch에서 1회 (서비스 코드와 일치)

        assertEquals(100, existing.getUsageValue());
    }
}