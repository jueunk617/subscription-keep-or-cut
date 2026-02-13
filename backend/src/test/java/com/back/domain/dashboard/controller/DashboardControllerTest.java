package com.back.domain.dashboard.controller;

import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.dashboard.service.DashboardService;
import com.back.domain.evaluation.enums.EvaluationStatus;
import com.back.global.exception.ErrorCode;
import com.back.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("월 대시보드 조회 성공")
    void t1() throws Exception {
        // given
        int year = 2026;
        int month = 2;

        DashboardResponse response = new DashboardResponse(
                46000L,
                102000L,
                List.of(
                        new DashboardResponse.SubscriptionSummary(
                                10L,
                                "OTT",
                                "Netflix",
                                50.0,
                                EvaluationStatus.REVIEW,
                                102000L,
                                false,
                                0L, // potentialAnnualWaste (ACTIVE면 0)
                                500L // costPerUnit 추가
                        )
                )
        );

        given(dashboardService.getMonthlyDashboard(year, month))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/dashboard")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.message").value(year + "년 " + month + "월 대시보드 조회 성공"))
                .andExpect(jsonPath("$.data.totalMonthlyCost").value(46000))
                .andExpect(jsonPath("$.data.totalAnnualWasteEstimate").value(102000))
                .andExpect(jsonPath("$.data.subscriptions.length()").value(1))
                .andExpect(jsonPath("$.data.subscriptions[0].trial").value(false))
                .andExpect(jsonPath("$.data.subscriptions[0].potentialAnnualWaste").value(0));
    }

    @Test
    @DisplayName("예외 - month가 1~12 범위를 벗어나면 BAD_REQUEST + fieldErrors 반환")
    void t2() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .param("year", "2026")
                        .param("month", "13")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.data[*].field").value(hasItem("month")))
                .andExpect(jsonPath("$.data[?(@.field=='month')].message").exists());

        then(dashboardService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예외 - year가 정책 범위를 벗어나면 BAD_REQUEST + fieldErrors 반환")
    void t3() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .param("year", "1800")
                        .param("month", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.data[*].field").value(hasItem("year")))
                .andExpect(jsonPath("$.data[?(@.field=='year')].message").exists());

        then(dashboardService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("예외 - 필수 파라미터 month 누락 시 BAD_REQUEST + fieldErrors 반환")
    void t4() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .param("year", "2026")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.data[*].field").value(hasItem("month")))
                .andExpect(jsonPath("$.data[?(@.field=='month')].message").value(hasItem("필수 파라미터입니다.")));

        then(dashboardService).shouldHaveNoInteractions();
    }
}
