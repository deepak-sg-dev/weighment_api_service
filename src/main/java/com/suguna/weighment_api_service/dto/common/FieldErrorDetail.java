package com.suguna.weighment_api_service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {

    private String field;
    private String message;
    private Object rejectedValue;
}
