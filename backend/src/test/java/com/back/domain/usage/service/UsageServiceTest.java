package com.back.domain.usage.service;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.evaluation.policy.EvaluationPolicy;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
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

    @Mock
    private EvaluationPolicy evaluationPolicy;

    @Test
    @DisplayName("사용량 및 평가 정상 생성")
    void t1() {
        // given
        YearMonth ym = YearMonth.of(2025, 1);

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

        UsageRequest request = new UsageRequest(1L, ym, 100);

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));
        given(usageRepository.findBySubscriptionAndUsageMonth(subscription, ym)).willReturn(Optional.empty());
        given(evaluationRepository.findBySubscriptionAndEvalMonth(subscription, ym)).willReturn(Optional.empty());

        given(evaluationPolicy.calculateStatus(anyDouble(), anyInt()))
                .willReturn(EvaluationStatus.REVIEW);

        given(usageRepository.saveAndFlush(any(SubscriptionUsage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(evaluationRepository.saveAndFlush(any(SubscriptionEvaluation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        verify(usageRepository, times(1)).saveAndFlush(any(SubscriptionUsage.class));
        verify(evaluationRepository, times(1)).saveAndFlush(any(SubscriptionEvaluation.class));
        verify(evaluationPolicy, atLeastOnce()).calculateStatus(anyDouble(), anyInt());
    }

    @Test
    @DisplayName("기존 사용량 존재 시 update 수행")
    void t2() {
        // given
        YearMonth ym = YearMonth.of(2025, 1);

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

        SubscriptionUsage existingUsage = new SubscriptionUsage(subscription, ym, 5);
        UsageRequest request = new UsageRequest(1L, ym, 20);

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));
        given(usageRepository.findBySubscriptionAndUsageMonth(subscription, ym)).willReturn(Optional.of(existingUsage));
        given(evaluationRepository.findBySubscriptionAndEvalMonth(subscription, ym)).willReturn(Optional.empty());

        given(evaluationPolicy.calculateStatus(anyDouble(), anyInt()))
                .willReturn(EvaluationStatus.REVIEW);

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
        verify(evaluationPolicy, atLeastOnce()).calculateStatus(anyDouble(), anyInt());
    }

    @Test
    @DisplayName("예외 발생 - 구독이 존재하지 않는 경우")
    void t3() {
        // given
        UsageRequest request = new UsageRequest(99L, YearMonth.of(2025, 1), 100);
        given(subscriptionRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> usageService.recordUsageAndEvaluate(request));

        // 구독 조회에서 바로 터지므로 하위 의존성 호출 없음
        verifyNoInteractions(usageRepository);
        verifyNoInteractions(evaluationRepository);
        verifyNoInteractions(evaluationPolicy);
    }

    @Test
    @DisplayName("예외 - 단위가 DAYS인 구독에서 usageValue가 해당 월 일수를 초과하면 예외 발생")
    void t4() {
        // given
        YearMonth ym = YearMonth.of(2025, 2); // 28일

        Category category = new Category("AI_TOOL", 15, UsageUnit.DAYS, CategoryType.PRODUCTIVITY);

        Subscription subscription = new Subscription(
                category,
                "ChatGPT Plus",
                29000L, 29000L, 29000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        UsageRequest request = new UsageRequest(1L, ym, 29); // 28 초과
        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        // when & then
        assertThrows(CustomException.class, () -> usageService.recordUsageAndEvaluate(request));

        // validate에서 터지므로 저장/평가 로직 진입 X
        verifyNoInteractions(usageRepository);
        verifyNoInteractions(evaluationRepository);
        verifyNoInteractions(evaluationPolicy);
    }

    @Test
    @DisplayName("동시성 상황 - 사용량 저장 중 유니크 충돌 발생 시 재조회 후 update 처리")
    void t5() {
        // given
        YearMonth ym = YearMonth.of(2025, 3);

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

        UsageRequest request = new UsageRequest(1L, ym, 100);

        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(subscription));

        // 최초 조회: 없음 -> try에서 insert 시도
        // catch 이후 재조회: 있음
        SubscriptionUsage afterConflict = new SubscriptionUsage(subscription, ym, 5);

        given(usageRepository.findBySubscriptionAndUsageMonth(subscription, ym))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(afterConflict));

        // saveAndFlush에서 유니크 충돌
        given(usageRepository.saveAndFlush(any(SubscriptionUsage.class)))
                .willThrow(new DataIntegrityViolationException("duplicate key"));

        // catch에서 save(existing) 수행
        given(usageRepository.save(any(SubscriptionUsage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // evaluation은 정상 플로우로만
        given(evaluationRepository.findBySubscriptionAndEvalMonth(subscription, ym))
                .willReturn(Optional.empty());

        given(evaluationPolicy.calculateStatus(anyDouble(), anyInt()))
                .willReturn(EvaluationStatus.REVIEW);

        given(evaluationRepository.saveAndFlush(any(SubscriptionEvaluation.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        usageService.recordUsageAndEvaluate(request);

        // then
        verify(usageRepository).saveAndFlush(any(SubscriptionUsage.class));
        verify(usageRepository).save(any(SubscriptionUsage.class));

        // 값 업데이트까지 확인 (catch에서 existing.updateValue 수행)
        assertEquals(100, afterConflict.getUsageValue());

        verify(evaluationRepository).saveAndFlush(any(SubscriptionEvaluation.class));
        verify(evaluationPolicy, atLeastOnce()).calculateStatus(anyDouble(), anyInt());
    }
}
