package com.back.domain.evaluation.entity;

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

    // 어떤 구독에 대한 평가인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "eval_year", nullable = false)
    private int year;

    @Column(name = "eval_month", nullable = false)
    private int month;

    // 효율 비율
    @Column(nullable = false)
    private double efficiencyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationStatus evaluationStatus;

    // 연간 낭비 추정 금액
    @Column(nullable = false)
    private int annualWasteEstimate;

    public SubscriptionEvaluation(Subscription subscription,
                                  int year,
                                  int month,
                                  double efficiencyRate,
                                  EvaluationStatus evaluationStatus,
                                  int annualWasteEstimate) {
        this.subscription = subscription;
        this.year = year;
        this.month = month;
        this.efficiencyRate = efficiencyRate;
        this.evaluationStatus = evaluationStatus;
        this.annualWasteEstimate = annualWasteEstimate;
    }
}
