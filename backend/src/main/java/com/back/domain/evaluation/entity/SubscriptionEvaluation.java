package com.back.domain.evaluation.entity;

import com.back.domain.category.enums.CategoryType;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.evaluation.policy.EvaluationPolicy;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "subscription_evaluation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subscription_id", "eval_month"})
        }
)
public class SubscriptionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "eval_month", nullable = false)
    private YearMonth evalMonth;

    private double efficiencyRate;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus status;

    private long annualWaste;

    private int referenceSnapshotValue;

    private long costPerUnit;

    public SubscriptionEvaluation(Subscription subscription, YearMonth evalMonth) {
        this.subscription = subscription;
        this.evalMonth = evalMonth;
    }

    public void update(int usageValue, EvaluationPolicy policy) {
        applyEvaluation(usageValue, policy);
    }

    private void applyEvaluation(int usageValue, EvaluationPolicy policy) {
        this.referenceSnapshotValue = subscription.getCategory().getReferenceValue();
        CategoryType type = subscription.getCategory().getType();
        long monthlyCost = subscription.getMonthlyShareCost();

        // 1) 효율 계산
        double rate = 0;
        if (this.referenceSnapshotValue > 0) {
            rate = (double) usageValue / this.referenceSnapshotValue * 100;
            if (type == CategoryType.PRODUCTIVITY) {
                rate = Math.min(rate, 100);
            }
        }
        this.efficiencyRate = rate;

        // 2) 단위당 비용 계산
        this.costPerUnit = (usageValue > 0)
                ? Math.round((double) monthlyCost / usageValue)
                : monthlyCost;

        // 3) 상태 계산(정책 위임)
        this.status = policy.calculateStatus(rate, usageValue);

        // 4) 연간 낭비 계산 (TRIAL 정책 반영)
        if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            this.annualWaste = 0;
            return;
        }

        this.annualWaste = (rate >= 100)
                ? 0
                : Math.round(monthlyCost * (1 - rate / 100) * 12);
    }
}
