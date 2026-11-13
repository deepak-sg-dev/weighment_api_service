package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleItemDTO {

    private String id;

    private Long scheduleNo;

    private Long branchId;

    private String traderId;

    private String vehicleNumber;

    private String farmerId;

    private String farmerName;

    private Double quantity;

    private String pickupDate;

    private String status;
}