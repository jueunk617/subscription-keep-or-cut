package com.back.domain.subscription.controller;

import com.back.domain.subscription.dto.SubscriptionRequest;
import com.back.domain.subscription.dto.SubscriptionResponse;
import com.back.domain.subscription.service.SubscriptionService;
import com.back.global.common.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 구독 등록
    @PostMapping
    public RsData<SubscriptionResponse> create(@RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return RsData.success("구독이 성공적으로 등록되었습니다.", response);
    }

    // 구독 전체 조회
    @GetMapping
    public RsData<List<SubscriptionResponse>> getAll() {
        List<SubscriptionResponse> responses = subscriptionService.getAllSubscriptions();
        return RsData.success("구독 목록 조회 성공", responses);
    }

    // 구독 삭제
    @DeleteMapping("/{id}")
    public RsData<Void> delete(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return RsData.success(id + "번 구독이 삭제되었습니다.");
    }
}