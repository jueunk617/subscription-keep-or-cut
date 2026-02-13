package com.back.domain.subscription.entity;

import com.back.domain.category.entity.Category;
import com.back.domain.evaluation.entity.SubscriptionEvaluation;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import com.back.domain.usage.entity.SubscriptionUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscription")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 서비스 이름
    @Column(nullable = false)
    private String name;

    // 전체 결제 금액 (원가)
    @Column(nullable = false)
    private long totalCost;

    // 사용자 부담 금액 (공유 결제 고려)
    @Column(nullable = false)
    private long userShareCost;

    // userShareCost를 결제 주기에 따라 월 기준으로 환산한 금액
    @Column(nullable = false)
    private long monthlyShareCost;

    // 결제 주기
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    // 현재 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SubscriptionUsage> usages = new ArrayList<>();

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SubscriptionEvaluation> evaluations = new ArrayList<>();

    public Subscription(Category category,
                        String name,
                        long totalCost,
                        long userShareCost,
                        long monthlyShareCost,
                        BillingCycle billingCycle,
                        SubscriptionStatus status) {
        this.category = category;
        this.name = name;
        this.totalCost = totalCost;
        this.userShareCost = userShareCost;
        this.monthlyShareCost = monthlyShareCost;
        this.billingCycle = billingCycle;
        this.status = status;
    }
}
