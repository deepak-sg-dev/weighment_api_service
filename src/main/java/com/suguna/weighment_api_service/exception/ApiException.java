package com.suguna.weighment_api_service.exception;

import com.suguna.weighment_api_service.constants.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), Map.of());
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public ApiException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : details;
    }

    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, Map.of(), cause);
    }

    public ApiException(ErrorCode errorCode, String message, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : details;
    }
}
