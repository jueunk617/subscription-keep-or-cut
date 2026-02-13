package com.back.global.exception.dto;

public record FieldErrorDto(
        String field,
        String message
) {}
