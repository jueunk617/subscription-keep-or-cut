package com.back.domain.subscription.entity;

import com.back.domain.category.entity.Category;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private int totalCost;

    // 사용자 부담 금액 (공유 결제 고려)
    @Column(nullable = false)
    private int userShareCost;

    // userShareCost를 결제 주기에 따라 월 기준으로 환산한 금액
    @Column(nullable = false)
    private int monthlyShareCost;

    // 결제 주기
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    // 현재 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    public Subscription(Category category,
                        String name,
                        int totalCost,
                        int userShareCost,
                        int monthlyShareCost,
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
