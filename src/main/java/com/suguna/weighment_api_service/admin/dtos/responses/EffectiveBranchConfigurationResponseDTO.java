package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EffectiveBranchConfigurationResponseDTO {

    private Long branchId;

    private Map<String, Object> effective;

    private Map<String, String> sources;
}