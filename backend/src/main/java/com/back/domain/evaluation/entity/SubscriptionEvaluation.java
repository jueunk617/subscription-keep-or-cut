package com.back.domain.evaluation.entity;

import com.back.domain.category.enums.CategoryType;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.domain.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "subscription_evaluation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subscription_id", "eval_year", "eval_month"})
        }
)
public class SubscriptionEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "eval_year")
    private int year;

    @Column(name = "eval_month")
    private int month;

    private double efficiencyRate;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus status;

    private long annualWaste;

    public SubscriptionEvaluation(Subscription subscription, int year, int month) {
        this.subscription = subscription;
        this.year = year;
        this.month = month;
    }

    // 사용량 기반 평가 수행
    public void evaluate(int usageValue) {

        int referenceValue = subscription.getCategory().getReferenceValue();
        CategoryType type = subscription.getCategory().getType();

        long monthlyCost = subscription.getMonthlyShareCost();

        // 1️. 효율 계산
        double rate = 0;

        if (referenceValue > 0) {
            rate = (double) usageValue / referenceValue * 100;

            if (type == CategoryType.PRODUCTIVITY) {
                rate = Math.min(rate, 100);
            }
        }

        this.efficiencyRate = rate;

        // 2️. 상태 계산
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

        // 3️. 연간 낭비 계산
        if (rate >= 100) {
            this.annualWaste = 0;
        } else {
            this.annualWaste = (long) (monthlyCost * (1 - rate / 100) * 12);
        }
    }

    public void update(int usageValue) {
        evaluate(usageValue);
    }
}
