package com.back.global.common.dto;

import com.back.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RsData<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> RsData<T> success(String message, T data) {
        return new RsData<>(true, "SUCCESS_200", message, data);
    }

    public static <T> RsData<T> success(String message) {
        return new RsData<>(true, "SUCCESS_200", message, null);
    }

    public static <T> RsData<T> fail(ErrorCode errorCode) {
        return new RsData<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> RsData<T> fail(ErrorCode errorCode, String customMessage) {
        return new RsData<>(false, errorCode.getCode(), customMessage, null);
    }

    public static <T> RsData<T> fail(ErrorCode errorCode, String customMessage, T data) {
        return new RsData<>(false, errorCode.getCode(), customMessage, data);
    }
}