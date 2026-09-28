package com.suguna.weighment_api_service.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupervisorMeResponse {

    private boolean success;
    private SupervisorDto supervisor;
}
