package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private int statusCode;

    private T data;

    private LocalDateTime timestamp;

}