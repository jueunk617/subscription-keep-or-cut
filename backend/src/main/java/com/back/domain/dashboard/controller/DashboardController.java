package com.back.domain.dashboard.controller;

import com.back.domain.dashboard.dto.DashboardResponse;
import com.back.domain.dashboard.service.DashboardService;
import com.back.global.common.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public RsData<DashboardResponse> getDashboard(
            @RequestParam int year,
            @RequestParam int month
    ) {
        DashboardResponse response = dashboardService.getMonthlyDashboard(year, month);
        return RsData.success(year + "년 " + month + "월 대시보드 조회 성공", response);
    }
}