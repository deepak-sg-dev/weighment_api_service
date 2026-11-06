package com.suguna.weighment_api_service.dto.auth;


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
public class SupervisorMeResponse {

    private boolean success;
    private SupervisorDto supervisor;
}