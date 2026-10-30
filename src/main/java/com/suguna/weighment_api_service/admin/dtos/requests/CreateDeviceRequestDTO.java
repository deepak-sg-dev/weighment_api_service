package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDeviceRequestDTO {

    private String imei;

    private Long branchId;

    private Long supervisorId;

    private String supervisorCode;

    private Long ledgerId;
}