package com.back.domain.usage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UsageRequest(
        @NotNull Long subscriptionId,
        @Min(2000) @Max(2100) int year,
        @Min(1) @Max(12) int month,
        @Min(0) int usageValue
) {}