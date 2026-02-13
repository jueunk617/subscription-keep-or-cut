package com.back.global.exception;

import com.back.global.common.dto.RsData;
import com.back.global.exception.dto.FieldErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED_MESSAGE = "요청 값 검증에 실패했습니다.";

    // 정의한 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<RsData<Void>> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("[CustomException] {} : {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(RsData.fail(errorCode));
    }

    // @Valid (RequestBody) 검증 실패 -> 필드 에러 리스트 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<List<FieldErrorDto>>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("[MethodArgumentNotValidException] : {}", ex.getMessage());

        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDto)
                .toList();

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, VALIDATION_FAILED_MESSAGE, fieldErrors));
    }

    // @Validated + @RequestParam/@PathVariable 검증 실패 -> 필드 에러 리스트 반환
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<List<FieldErrorDto>>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("[ConstraintViolationException] : {}", ex.getMessage());

        List<FieldErrorDto> fieldErrors = ex.getConstraintViolations().stream()
                .map(this::toFieldErrorDto)
                .toList();

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, VALIDATION_FAILED_MESSAGE, fieldErrors));
    }

    // 필수 RequestParam 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RsData<List<FieldErrorDto>>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.warn("[MissingServletRequestParameterException] : {}", ex.getMessage());

        List<FieldErrorDto> fieldErrors = List.of(
                new FieldErrorDto(ex.getParameterName(), "필수 파라미터입니다.")
        );

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(RsData.fail(ErrorCode.BAD_REQUEST, VALIDATION_FAILED_MESSAGE, fieldErrors));
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


    // ========================= 변환 유틸 =========================

    private FieldErrorDto toFieldErrorDto(FieldError fe) {
        return new FieldErrorDto(fe.getField(), fe.getDefaultMessage());
    }

    private FieldErrorDto toFieldErrorDto(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        String field = extractLastPathToken(path);
        String msg = v.getMessage();

        return new FieldErrorDto(field.isBlank() ? path : field, msg);
    }

    private String extractLastPathToken(String path) {
        if (path == null || path.isBlank()) return "";
        int idx = path.lastIndexOf('.');
        return (idx >= 0 && idx < path.length() - 1) ? path.substring(idx + 1) : path;
    }
}