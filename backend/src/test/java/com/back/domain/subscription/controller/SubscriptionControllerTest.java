package com.back.domain.subscription.controller;

import com.back.domain.subscription.dto.SubscriptionRequest;
import com.back.domain.subscription.dto.SubscriptionResponse;
import com.back.domain.subscription.enums.BillingCycle;
import com.back.domain.subscription.enums.SubscriptionStatus;
import com.back.domain.subscription.service.SubscriptionService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@Import(GlobalExceptionHandler.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("구독 생성 API 성공")
    void t1() throws Exception {
        // given
        SubscriptionRequest request = new SubscriptionRequest(
                1L,
                "Netflix",
                15000,
                5000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        SubscriptionResponse response = new SubscriptionResponse(
                10L,
                "OTT",
                "Netflix",
                5000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        given(subscriptionService.createSubscription(any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.message").value("구독이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.name").value("Netflix"))
                .andExpect(jsonPath("$.data.monthlyShareCost").value(5000));

        verify(subscriptionService).createSubscription(any());
    }

    @Test
    @DisplayName("구독 목록 조회 API 성공")
    void t2() throws Exception {
        // given
        SubscriptionResponse response = new SubscriptionResponse(
                1L,
                "OTT",
                "Netflix",
                5000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        given(subscriptionService.getAllSubscriptions())
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.message").value("구독 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Netflix"))
                .andExpect(jsonPath("$.data[0].monthlyShareCost").value(5000));

        verify(subscriptionService).getAllSubscriptions();
    }

    @Test
    @DisplayName("구독 삭제 API 성공")
    void t3() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.message").value("1번 구독이 삭제되었습니다."));

        verify(subscriptionService).deleteSubscription(1L);
    }

    @Test
    @DisplayName("예외 발생 - 존재하지 않는 카테고리 (구독 생성)")
    void t4() throws Exception {
        // given
        SubscriptionRequest request = new SubscriptionRequest(
                99L,
                "Netflix",
                15000,
                5000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        given(subscriptionService.createSubscription(any()))
                .willThrow(new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.CATEGORY_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CATEGORY_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(subscriptionService).createSubscription(any());
    }

    @Test
    @DisplayName("예외 발생 - 존재하지 않는 구독 삭제")
    void t5() throws Exception {

        willThrow(new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND))
                .given(subscriptionService)
                .deleteSubscription(1L);

        mockMvc.perform(delete("/api/v1/subscriptions/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.SUBSCRIPTION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage()));

        verify(subscriptionService).deleteSubscription(1L);
    }

    @Test
    @DisplayName("예외 발생 - name이 빈 값이면 BAD_REQUEST")
    void t6() throws Exception {
        SubscriptionRequest invalidRequest = new SubscriptionRequest(
                1L,
                "",
                15000,
                5000,
                BillingCycle.MONTHLY,
                SubscriptionStatus.ACTIVE
        );

        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));

        verify(subscriptionService, never()).createSubscription(any());
    }
}
