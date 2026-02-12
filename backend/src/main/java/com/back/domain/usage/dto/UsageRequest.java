package com.back.domain.usage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UsageRequest(
        @NotNull Long subscriptionId,
        @NotNull int year,
        @NotNull @Min(1) int month,
        @NotNull @Min(0) int usageValue
) {}