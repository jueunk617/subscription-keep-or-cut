package com.back.domain.subscription.service;

import com.back.domain.category.entity.Category;
import com.back.domain.category.repository.CategoryRepository;
import com.back.domain.subscription.dto.SubscriptionRequest;
import com.back.domain.subscription.dto.SubscriptionResponse;
import com.back.domain.subscription.entity.Subscription;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.repository.SubscriptionRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 새로운 구독 정보를 생성합니다.
     */
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 가상 월 비용 계산
        int virtualMonthlyCost = calculateVirtualMonthlyCost(request.userShareCost(), request.billingCycle());

        // 3. 엔티티 생성 및 저장
        Subscription subscription = new Subscription(
                category,
                request.name(),
                virtualMonthlyCost,
                request.billingCycle(),
                request.status()
        );

        Subscription saved = subscriptionRepository.save(subscription);

        return toResponse(saved);
    }

    /**
     * 모든 구독 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 구독 정보를 삭제합니다.
     */
    @Transactional
    public void deleteSubscription(Long id) {
        // 존재 여부 확인 후 삭제
        if (!subscriptionRepository.existsById(id)) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        subscriptionRepository.deleteById(id);
    }

    // 엔티티 -> Response Record 변환
    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getCategory().getName(),
                s.getName(),
                s.getVirtualMonthlyCost(),
                s.getBillingCycle(),
                s.getStatus()
        );
    }

    // 결제 주기에 따른 환산 로직
    private int calculateVirtualMonthlyCost(int shareCost, BillingCycle cycle) {
        return switch (cycle) {
            case ANNUAL -> shareCost / 12;
            case QUARTERLY -> shareCost / 3;
            case MONTHLY -> shareCost;
        };
    }
}