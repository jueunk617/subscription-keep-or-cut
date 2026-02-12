package com.back.domain.usage.controller;

import com.back.domain.usage.dto.UsageRequest;
import com.back.domain.usage.service.UsageService;
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

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsageController.class)
@Import(GlobalExceptionHandler.class)
class UsageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsageService usageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용량 기록 API 성공")
    void t1() throws Exception {
        UsageRequest request = new UsageRequest(1L, 2025, 2, 80);

        mockMvc.perform(post("/api/v1/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("사용량이 기록되었으며 효율 분석이 완료되었습니다."));
    }

    @Test
    @DisplayName("구독이 존재하지 않으면 예외 응답 반환")
    void t2() throws Exception {
        UsageRequest request = new UsageRequest(99L, 2025, 2, 80);

        willThrow(new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND))
                .given(usageService)
                .recordUsageAndEvaluate(any());

        mockMvc.perform(post("/api/v1/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code")
                        .value(ErrorCode.SUBSCRIPTION_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("예외 - usageValue가 음수면 BAD_REQUEST")
    void t3() throws Exception {
        UsageRequest request = new UsageRequest(1L, 2025, 2, -1);

        mockMvc.perform(post("/api/v1/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));

        then(usageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예외 - month가 1~12 범위를 벗어나면 BAD_REQUEST")
    void t4() throws Exception {
        UsageRequest request = new UsageRequest(1L, 2025, 13, 10);

        mockMvc.perform(post("/api/v1/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));

        then(usageService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예외 - subscriptionId가 null이면 BAD_REQUEST")
    void t5() throws Exception {
        String invalidJson = """
                {
                  "subscriptionId": null,
                  "year": 2025,
                  "month": 2,
                  "usageValue": 10
                }
                """;

        mockMvc.perform(post("/api/v1/usages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));

        then(usageService).shouldHaveNoInteractions();
    }
}
