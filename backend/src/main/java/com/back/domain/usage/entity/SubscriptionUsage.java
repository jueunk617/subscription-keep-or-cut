package com.back.domain.usage.entity;

import com.back.domain.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "subscription_usage",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"subscription_id", "usage_year", "usage_month"})
        }
)
public class SubscriptionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "usage_year", nullable = false)
    private int year;

    @Column(name = "usage_month", nullable = false)
    private int month;

    @Column(nullable = false)
    private int usageValue;

    public SubscriptionUsage(Subscription subscription, int year, int month, int usageValue) {
        this.subscription = subscription;
        this.year = year;
        this.month = month;
        this.usageValue = usageValue;
    }

    public void updateValue(int usageValue) {
        this.usageValue = usageValue;
    }
}