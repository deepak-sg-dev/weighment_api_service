package com.suguna.weighment_api_service.admin.response;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class Response {

    public static <T> ResponseEntity<ApiResponse<T>> success(
            String message, T data) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(
            String message, HttpStatus status) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .statusCode(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }


}

