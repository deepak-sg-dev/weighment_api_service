package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleItemDTO {

    private String id;

    private String scheduleNo;

    private Long branchId;

    private String branch;

    private String supervisorId;

    private String supervisor;

    private String traderId;

    private String trader;

    private String traderName;

    private String depot;

    private String vehicleNumber;

    private String vehicle;

    private String farmerId;

    private String farmerName;

    private Double quantity;

    private String source;

    private String pickupDate;

    private String status;

    // Additional Fields

    private String birdType;

    private String saleType;

    private String scaleType;

    private String deliveryStatus;

    private Double plannedQuantity;

    private String scheduledAt;

    private String startedAt;

    private Integer orderCount;

    private String customerLocation;

    private String farmLocation;
}