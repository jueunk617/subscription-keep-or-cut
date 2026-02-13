package com.back.domain.subscription.service;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
import com.back.domain.category.repository.CategoryRepository;
import com.back.domain.subscription.dto.SubscriptionRequest;
import com.back.domain.subscription.dto.SubscriptionResponse;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import com.back.domain.subscription.repository.SubscriptionRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("구독 정상 생성")
    void t1() {
        // given
        Category category = new Category(
                "OTT",
                1,
                UsageUnit.DAYS,
                CategoryType.CONTENT
        );

        ReflectionTestUtils.setField(category, "id", 1L);

        SubscriptionRequest request = new SubscriptionRequest(
                1L,
                "Netflix",
                15000L,
                5000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        given(categoryRepository.findById(1L))
                .willReturn(Optional.of(category));

        given(subscriptionRepository.save(any(Subscription.class)))
                .willAnswer(invocation -> {
                    Subscription s = invocation.getArgument(0);
                    ReflectionTestUtils.setField(s, "id", 10L);
                    return s;
                });

        // when
        SubscriptionResponse response = subscriptionService.createSubscription(request);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Netflix");
        assertThat(response.monthlyShareCost()).isEqualTo(5000L);
        assertThat(response.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("연간 결제의 경우 월 환산 사용자 부담금 계산")
    void t2() {
        // given
        Category category = new Category(
                "OTT",
                1,
                UsageUnit.DAYS,
                CategoryType.CONTENT
        );

        ReflectionTestUtils.setField(category, "id", 1L);

        SubscriptionRequest request = new SubscriptionRequest(
                1L,
                "Netflix",
                120000L,
                120000L,
                BillingCycle.ANNUAL,
                SubscriptionStatus.ACTIVE
        );

        given(categoryRepository.findById(1L))
                .willReturn(Optional.of(category));

        given(subscriptionRepository.save(any(Subscription.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        SubscriptionResponse response = subscriptionService.createSubscription(request);

        // then
        assertThat(response.monthlyShareCost()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("예외 발생 - 카테고리 없는 경우")
    void t3() {
        // given
        SubscriptionRequest request = new SubscriptionRequest(
                99L,
                "Netflix",
                15000L,
                5000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        given(categoryRepository.findById(99L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.createSubscription(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("예외 발생 - 존재하지 않는 구독 삭제")
    void t4() {
        // given
        given(subscriptionRepository.existsById(1L))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> subscriptionService.deleteSubscription(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("구독 정상 삭제")
    void t5() {
        // given
        given(subscriptionRepository.existsById(1L))
                .willReturn(true);

        // when
        subscriptionService.deleteSubscription(1L);

        // then
        verify(subscriptionRepository).deleteById(1L);
    }
}
