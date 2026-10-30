package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupervisorResponseDTO {

    private String supervisorId;

    private String name;

    private Long branchId;

    private String mobile;

    private String deviceImei;

    private String status;
}