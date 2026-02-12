package com.back.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ======================== 공통 에러 ========================
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_003", "권한이 없습니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_004", "날짜 형식이 올바르지 않습니다 (예: YYYY-MM-DD)."),

    // ======================== 카테고리 관련 ========================
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_001", "존재하지 않는 카테고리입니다."),

    // ======================== 구독 관련 ========================
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "존재하지 않는 구독 정보입니다."),

    // ======================== 사용량 관련 ========================
    USAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "USAGE_001", "해당 월의 사용량 기록을 찾을 수 없습니다."),
    USAGE_ALREADY_EXISTS(HttpStatus.CONFLICT, "USAGE_002", "해당 월의 사용량 데이터가 이미 존재합니다."),
    INVALID_USAGE_VALUE(HttpStatus.BAD_REQUEST, "USAGE_003", "사용량 값은 음수일 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}