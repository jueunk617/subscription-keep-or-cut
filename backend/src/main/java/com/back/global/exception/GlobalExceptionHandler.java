package com.back.global.exception;

import com.back.global.common.dto.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 정의한 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<RsData<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[CustomException] {} : {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RsData.fail(errorCode));
    }

    // Validation 예외 처리 (@Valid 관련)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("[ValidationException] : {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST));
    }

    // 그 외 예상치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleGenericException(Exception ex) {
        log.error("[InternalServerError] ", ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(RsData.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // 파비콘 등을 찾지 못할 때는 그냥 아무것도 안 하고 넘어가게 함 (로그 클리닝)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public void handleNoResourceFoundException() { }
}