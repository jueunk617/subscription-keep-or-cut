package com.back.domain.usage.entity;

import com.back.domain.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "subscription_usage",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subscription_id", "usage_month"})
        }
)
public class SubscriptionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "usage_month", nullable = false)
    private YearMonth usageMonth;

    @Column(nullable = false)
    private int usageValue;

    public SubscriptionUsage(Subscription subscription, YearMonth usageMonth, int usageValue) {
        this.subscription = subscription;
        this.usageMonth = usageMonth;
        this.usageValue = usageValue;
    }

    public void updateValue(int usageValue) {
        this.usageValue = usageValue;
    }
}