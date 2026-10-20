package com.suguna.weighment_api_service.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.suguna.weighment_api_service.constants.ApiConstants;
import com.suguna.weighment_api_service.constants.ErrorCode;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Standard envelope for Flutter integration.
 * <p>
 * Success: {@code success}, {@code data}, {@code message}, {@code timestamp}<br>
 * Error: {@code success}, {@code errorCode}, {@code message}, {@code details}, {@code timestamp}
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private Instant timestamp;

    /** Present only on error responses. */
    private String errorCode;

    /** Optional structured context (field errors, OTP hints, etc.). */
    private Map<String, Object> details;

    /** Optional debug path when {@code app.api.response.include-path=true}. */
    private String path;

    public static <T> CommonApiResponse<T> success(T data) {
        return success(data, ApiConstants.DEFAULT_SUCCESS_MESSAGE);
    }

    public static <T> CommonApiResponse<T> success(T data, String message) {
        CommonApiResponse<T> response = new CommonApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        response.setTimestamp(Instant.now());
        return response;
    }

    public static CommonApiResponse<Map<String, Object>> successEmpty() {
        return success(Map.of());
    }

    public static <T> CommonApiResponse<T> error(
            String errorCode, String message, Map<String, Object> details, String path) {
        CommonApiResponse<T> response = new CommonApiResponse<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        response.setDetails(details == null ? Map.of() : details);
        response.setPath(path);
        response.setTimestamp(Instant.now());
        return response;
    }

    public static <T> CommonApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDefaultMessage(), Map.of(), null);
    }

    public static <T> CommonApiResponse<T> error(ErrorCode errorCode, String message) {
        return error(errorCode, message, Map.of(), null);
    }

    public static <T> CommonApiResponse<T> error(
            ErrorCode errorCode, String message, Map<String, Object> details, String path) {
        return error(errorCode.getErrorCode(), message, details, path);
    }
}
