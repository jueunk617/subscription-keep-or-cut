package com.back.domain.usage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record UsageRequest(
        @NotNull
        Long subscriptionId,

        @NotNull(message = "연/월 정보는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM")
        YearMonth date,

        @Min(0)
        int usageValue
) {}