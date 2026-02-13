package com.back.domain.evaluation.entity;

import com.back.domain.category.entity.Category;
import com.back.domain.category.enums.CategoryType;
import com.back.domain.category.enums.UsageUnit;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionEvaluationTest {

    private Subscription createNetflixSubscription() {
        Category ott = new Category(
                "OTT",
                1200,
                UsageUnit.MINUTES,
                CategoryType.CONTENT
        );

        return new Subscription(
                ott,
                "Netflix",
                17000L, // totalCost
                17000L, // userShareCost
                17000L, // monthlyShareCost
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );
    }

    private Subscription createChatGptSubscription() {
        Category aiTool = new Category(
                "AI_TOOL",
                15,
                UsageUnit.DAYS,
                CategoryType.PRODUCTIVITY
        );

        return new Subscription(
                aiTool,
                "ChatGPT Plus",
                29000L,
                29000L,
                29000L,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("넷플릭스를 한 달 동안 전혀 시청하지 않으면 GHOST 상태가 된다")
    void t1() {
        Subscription subscription = createNetflixSubscription();
        SubscriptionEvaluation evaluation =
                new SubscriptionEvaluation(subscription, 2025, 2);

        evaluation.evaluate(0);

        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.GHOST);
        assertThat(evaluation.getAnnualWaste()).isEqualTo(17000L * 12);
    }

    @Test
    @DisplayName("넷플릭스를 월 기준만큼 시청하면 EFFICIENT 상태가 된다")
    void t2() {
        Subscription subscription = createNetflixSubscription();
        SubscriptionEvaluation evaluation =
                new SubscriptionEvaluation(subscription, 2025, 2);

        evaluation.evaluate(1200);

        assertThat(evaluation.getEfficiencyRate()).isEqualTo(100.0);
        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.EFFICIENT);
        assertThat(evaluation.getAnnualWaste()).isEqualTo(0L);
    }

    @Test
    @DisplayName("넷플릭스를 600분 시청하면 REVIEW 상태가 된다 (50%)")
    void t3() {
        Subscription subscription = createNetflixSubscription();
        SubscriptionEvaluation evaluation =
                new SubscriptionEvaluation(subscription, 2025, 2);

        evaluation.evaluate(600); // 50%

        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.REVIEW);
    }

    @Test
    @DisplayName("ChatGPT Plus를 기준일 이상 사용해도 효율은 100%를 초과하지 않는다")
    void t4() {
        Subscription subscription = createChatGptSubscription();
        SubscriptionEvaluation evaluation =
                new SubscriptionEvaluation(subscription, 2025, 2);

        evaluation.evaluate(30); // 기준 15일 → 200%

        assertThat(evaluation.getEfficiencyRate()).isEqualTo(100.0);
        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.EFFICIENT);
    }

    @Test
    @DisplayName("ChatGPT Plus를 5일만 사용하면 INEFFICIENT 상태가 된다")
    void t5() {
        Subscription subscription = createChatGptSubscription();
        SubscriptionEvaluation evaluation =
                new SubscriptionEvaluation(subscription, 2025, 2);

        evaluation.evaluate(5); // 약 33%

        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.INEFFICIENT);
    }
}
