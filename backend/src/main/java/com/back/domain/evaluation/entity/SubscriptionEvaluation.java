package com.back.domain.evaluation.entity;

import com.back.domain.category.enums.CategoryType;
import com.back.domain.evaluation.enums.EvaluationStatus;
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

    // 사용량 기반 평가 수행
    public void evaluate(int usageValue) {
        // 평가 시점의 기준값을 스냅샷으로 저장하여 이력 관리 보장
        this.referenceSnapshotValue = subscription.getCategory().getReferenceValue();
        CategoryType type = subscription.getCategory().getType();
        long monthlyCost = subscription.getMonthlyShareCost();

        // 1. 효율 계산
        double rate = 0;
        if (this.referenceSnapshotValue > 0) {
            rate = (double) usageValue / this.referenceSnapshotValue * 100;
            if (type == CategoryType.PRODUCTIVITY) {
                rate = Math.min(rate, 100);
            }
        }
        this.efficiencyRate = rate;

        // 2. 단위당 비용 계산
        if (usageValue > 0) {
            this.costPerUnit = Math.round((double) monthlyCost / usageValue);
        } else {
            this.costPerUnit = monthlyCost; // 사용량이 0이면 한 달 비용 전체가 낭비 단가
        }

        // 3. 상태 계산
        if (usageValue == 0) {
            this.status = EvaluationStatus.GHOST;
        } else if (rate >= 100) {
            this.status = EvaluationStatus.EFFICIENT;
        } else if (rate >= 70) {
            this.status = EvaluationStatus.KEEP;
        } else if (rate >= 40) {
            this.status = EvaluationStatus.REVIEW;
        } else {
            this.status = EvaluationStatus.INEFFICIENT;
        }

        // 4. 연간 낭비 계산 (TRIAL 정책 반영)
        if (subscription.getStatus() == SubscriptionStatus.TRIAL) {
            this.annualWaste = 0;
            return;
        }

        if (rate >= 100) {
            this.annualWaste = 0;
        } else {
            this.annualWaste = Math.round(monthlyCost * (1 - rate / 100) * 12);
        }
    }

    public void update(int usageValue) {
        evaluate(usageValue);
    }
}
