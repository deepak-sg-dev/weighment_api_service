package com.suguna.weighment_api_service.dto.common;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FieldErrorDetail {

    private String field;
    private String message;
    private Object rejectedValue;
}