package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceDetailResponseDTO {

    private String id;

    private String imei;

    private Long branchId;

    private Long supervisorId;

    private String status;
}