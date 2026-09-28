package com.suguna.weighment_api_service.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupervisorProfile {

    private final String id;
    private final String code;
    private final String name;
    private final String mobile;
    private final String branchId;
    private final String branchName;
    private final boolean active;
}
