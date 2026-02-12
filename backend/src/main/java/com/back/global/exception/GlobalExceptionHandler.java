package com.back.global.exception;

import com.back.global.common.dto.RsData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    // @Validated + @RequestParam/@PathVariable 예외 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("[ConstraintViolationException] : {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST));
    }

    // 필수 RequestParam 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RsData<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("[MissingServletRequestParameterException] : {}", ex.getMessage());
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

    // 정적 리소스 (파비콘 등) 요청 실패는 로그를 지저분하게 만들 수 있어 무시
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public void handleNoResourceFoundException() { }
}