package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.constants.ApiConstants;
import com.suguna.weighment_api_service.dto.common.CommonApiResponse;
import com.suguna.weighment_api_service.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<CommonApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(CommonApiResponse.success(data));
    }

    public static <T> ResponseEntity<CommonApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(CommonApiResponse.success(data, message));
    }

    public static ResponseEntity<CommonApiResponse<Map<String, Object>>> okEmpty() {
        return ResponseEntity.ok(CommonApiResponse.successEmpty());
    }

    public static <T> ResponseEntity<CommonApiResponse<PageResponse<T>>> ok(Page<T> page) {
        return ok(PageResponse.from(page));
    }

    public static <T> ResponseEntity<CommonApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonApiResponse.success(data, ApiConstants.DEFAULT_SUCCESS_MESSAGE));
    }
}
