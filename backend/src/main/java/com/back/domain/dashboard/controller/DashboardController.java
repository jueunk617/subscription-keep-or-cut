package com.back.domain.dashboard.controller;

import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.dashboard.service.DashboardService;
import com.back.global.common.dto.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public RsData<DashboardResponse> getDashboard(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {
        DashboardResponse response = dashboardService.getMonthlyDashboard(year, month);
        return RsData.success(year + "년 " + month + "월 대시보드 조회 성공", response);
    }
}