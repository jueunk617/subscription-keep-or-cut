package com.back.domain.dashboard.service;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.evaluation.repository.SubscriptionEvaluationRepository;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SubscriptionEvaluationRepository evaluationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("대시보드 정상 조회 - 월 지출 합계/연간 낭비 합계/구독 요약 리스트 반환")
    void t1() {
        // given
        int year = 2026;
        int month = 2;

        Category ott = new Category("OTT", 1800, UsageUnit.MINUTES, CategoryType.CONTENT);
        ReflectionTestUtils.setField(ott, "id", 1L);

        Subscription netflix = new Subscription(
                ott,
                "Netflix",
                17000L,
                17000L,
                17000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );
        ReflectionTestUtils.setField(netflix, "id", 10L);

        Category aiTool = new Category("AI_TOOL", 12, UsageUnit.DAYS, CategoryType.PRODUCTIVITY);
        ReflectionTestUtils.setField(aiTool, "id", 2L);

        Subscription chatgpt = new Subscription(
                aiTool,
                "ChatGPT Plus",
                29000L,
                29000L,
                29000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );
        ReflectionTestUtils.setField(chatgpt, "id", 11L);

        SubscriptionEvaluation eval1 = new SubscriptionEvaluation(netflix, year, month);
        ReflectionTestUtils.setField(eval1, "efficiencyRate", 50.0);
        ReflectionTestUtils.setField(eval1, "status", EvaluationStatus.REVIEW);
        ReflectionTestUtils.setField(eval1, "annualWaste", 102000L);

        SubscriptionEvaluation eval2 = new SubscriptionEvaluation(chatgpt, year, month);
        ReflectionTestUtils.setField(eval2, "efficiencyRate", 100.0);
        ReflectionTestUtils.setField(eval2, "status", EvaluationStatus.EFFICIENT);
        ReflectionTestUtils.setField(eval2, "annualWaste", 0L);

        given(evaluationRepository.findAllWithSubscriptionAndCategoryByYearAndMonth(year, month))
                .willReturn(List.of(eval1, eval2));

        // when
        DashboardResponse response = dashboardService.getMonthlyDashboard(year, month);

        // then
        assertThat(response.totalMonthlyCost()).isEqualTo(17000L + 29000L);
        assertThat(response.totalAnnualWasteEstimate()).isEqualTo(102000L);

        assertThat(response.subscriptions()).hasSize(2);

        DashboardResponse.SubscriptionSummary s1 = response.subscriptions().get(0);
        assertThat(s1.id()).isEqualTo(10L);
        assertThat(s1.categoryName()).isEqualTo("OTT");
        assertThat(s1.name()).isEqualTo("Netflix");
        assertThat(s1.efficiencyRate()).isEqualTo(50.0);
        assertThat(s1.status()).isEqualTo(EvaluationStatus.REVIEW);
        assertThat(s1.annualWaste()).isEqualTo(102000L);

        // ACTIVE는 potential 0
        assertThat(s1.trial()).isFalse();
        assertThat(s1.potentialAnnualWaste()).isEqualTo(0L);

        DashboardResponse.SubscriptionSummary s2 = response.subscriptions().get(1);
        assertThat(s2.id()).isEqualTo(11L);
        assertThat(s2.categoryName()).isEqualTo("AI_TOOL");
        assertThat(s2.name()).isEqualTo("ChatGPT Plus");
        assertThat(s2.efficiencyRate()).isEqualTo(100.0);
        assertThat(s2.status()).isEqualTo(EvaluationStatus.EFFICIENT);
        assertThat(s2.annualWaste()).isEqualTo(0L);

        assertThat(s2.trial()).isFalse();
        assertThat(s2.potentialAnnualWaste()).isEqualTo(0L);
    }

    @Test
    @DisplayName("해당 월 평가 데이터가 없으면 월 지출 합계/낭비 합계는 0이고 구독 요약은 빈 리스트")
    void t2() {
        // given
        int year = 2026;
        int month = 2;

        given(evaluationRepository.findAllWithSubscriptionAndCategoryByYearAndMonth(year, month))
                .willReturn(List.of());

        // when
        DashboardResponse response = dashboardService.getMonthlyDashboard(year, month);

        // then
        assertThat(response.totalMonthlyCost()).isEqualTo(0L);
        assertThat(response.totalAnnualWasteEstimate()).isEqualTo(0L);
        assertThat(response.subscriptions()).isEmpty();
    }

    @Test
    @DisplayName("TRIAL 구독은 월 비용/연간 낭비는 0이지만 잠재 낭비(potentialAnnualWaste)는 계산된다")
    void t3() {
        // given
        int year = 2026;
        int month = 2;

        Category ott = new Category("OTT", 1800, UsageUnit.MINUTES, CategoryType.CONTENT);
        ReflectionTestUtils.setField(ott, "id", 1L);

        Subscription netflixTrial = new Subscription(
                ott,
                "Netflix Trial",
                17000L,
                17000L,
                17000L, // 유료 전환 시 월 기준 비용
                BillingCycle.MONTHLY,
                SubscriptionStatus.TRIAL
        );
        ReflectionTestUtils.setField(netflixTrial, "id", 10L);

        SubscriptionEvaluation eval = new SubscriptionEvaluation(netflixTrial, year, month);
        ReflectionTestUtils.setField(eval, "efficiencyRate", 50.0);
        ReflectionTestUtils.setField(eval, "status", EvaluationStatus.REVIEW);
        ReflectionTestUtils.setField(eval, "annualWaste", 0L); // TRIAL 정책에 의해 실제 낭비는 0

        given(evaluationRepository.findAllWithSubscriptionAndCategoryByYearAndMonth(year, month))
                .willReturn(List.of(eval));

        // when
        DashboardResponse response = dashboardService.getMonthlyDashboard(year, month);

        // then
        assertThat(response.totalMonthlyCost()).isEqualTo(0L);
        assertThat(response.totalAnnualWasteEstimate()).isEqualTo(0L);

        assertThat(response.subscriptions()).hasSize(1);

        DashboardResponse.SubscriptionSummary s = response.subscriptions().get(0);
        assertThat(s.id()).isEqualTo(10L);
        assertThat(s.categoryName()).isEqualTo("OTT");
        assertThat(s.name()).isEqualTo("Netflix Trial");
        assertThat(s.efficiencyRate()).isEqualTo(50.0);
        assertThat(s.status()).isEqualTo(EvaluationStatus.REVIEW);
        assertThat(s.annualWaste()).isEqualTo(0L);

        assertThat(s.trial()).isTrue();

        // 잠재 낭비: 17000 * (1 - 0.5) * 12 = 102000
        assertThat(s.potentialAnnualWaste()).isEqualTo(102000L);
    }
}
