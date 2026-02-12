package com.back.domain.usage.controller;

import com.back.domain.usage.dto.UsageRequest;
import com.back.domain.usage.service.UsageService;
import com.back.global.common.dto.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/usages")
public class UsageController {

    private final UsageService usageService;

    @PostMapping
    public RsData<Void> record(@RequestBody UsageRequest request) {
        usageService.recordUsageAndEvaluate(request);
        return RsData.success("사용량이 기록되었으며 효율 분석이 완료되었습니다.");
    }
}