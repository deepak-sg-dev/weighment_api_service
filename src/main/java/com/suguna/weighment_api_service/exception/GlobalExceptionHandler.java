package com.suguna.weighment_api_service.exception;

import com.suguna.weighment_api_service.config.ApiResponseProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.dto.common.CommonApiResponse;
import com.suguna.weighment_api_service.dto.common.FieldErrorDetail;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String FIELD_ERRORS_KEY = "fieldErrors";

    private final ApiResponseProperties responseProperties;

    public GlobalExceptionHandler(ApiResponseProperties responseProperties) {
        this.responseProperties = responseProperties;
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBusinessValidation(
            BusinessValidationException ex,
            WebRequest request) {
        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDetails(),
                request);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleApiException(ApiException ex, WebRequest request) {
        return buildErrorResponse(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getErrorCode(),
                ex.getMessage(),
                ex.getDetails(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .toList();
        Map<String, Object> details = new HashMap<>();
        details.put(FIELD_ERRORS_KEY, fieldErrors);
        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED.getHttpStatus(),
                ErrorCode.VALIDATION_FAILED.getErrorCode(),
                ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                details,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> FieldErrorDetail.builder()
                        .field(v.getPropertyPath().toString())
                        .message(v.getMessage())
                        .rejectedValue(v.getInvalidValue())
                        .build())
                .toList();
        Map<String, Object> details = new HashMap<>();
        details.put(FIELD_ERRORS_KEY, fieldErrors);
        return buildErrorResponse(
                ErrorCode.VALIDATION_FAILED.getHttpStatus(),
                ErrorCode.VALIDATION_FAILED.getErrorCode(),
                ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                details,
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        log.debug("Unreadable request body: {}", ex.getMessage());
        return buildErrorResponse(
                ErrorCode.INVALID_JSON.getHttpStatus(),
                ErrorCode.INVALID_JSON.getErrorCode(),
                ErrorCode.INVALID_JSON.getDefaultMessage(),
                Map.of(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        String message = responseProperties.isMaskInternalErrors()
                ? ErrorCode.INTERNAL_ERROR.getDefaultMessage()
                : ex.getMessage();
        return buildErrorResponse(
                ErrorCode.INTERNAL_ERROR.getHttpStatus(),
                ErrorCode.INTERNAL_ERROR.getErrorCode(),
                message,
                Map.of(),
                request);
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return FieldErrorDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<CommonApiResponse<Void>> buildErrorResponse(
            HttpStatus httpStatus,
            String errorCode,
            String message,
            Map<String, Object> details,
            WebRequest request) {
        CommonApiResponse.CommonApiResponseBuilder<Void> builder = CommonApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .details(details == null || details.isEmpty() ? Map.of() : details);

        if (responseProperties.isIncludeTimestamp()) {
            builder.timestamp(Instant.now());
        }
        if (responseProperties.isIncludePath()) {
            builder.path(resolveRequestPath(request));
        }

        return ResponseEntity.status(httpStatus).body(builder.build());
    }

    private String resolveRequestPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}
