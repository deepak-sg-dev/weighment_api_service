package com.suguna.weighment_api_service.domain;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LiftingSchedule {

    private String scheduleId;
    private String supervisorId;
    private String farmId;
    private String farmName;
    private double farmLatitude;
    private double farmLongitude;
    private int geofenceRadiusMeters;
    private String farmerId;
    private String farmerName;
    private String farmerMobile;
    private String traderId;
    private String traderName;
    private String traderMobile;
    private String productId;
    private String productName;
    private int plannedQuantity;
    private String operationType;
    private String scaleType;
    private ScheduleStatus status;
    private boolean downloaded;
    private int standardBirdsPerCage;
    private long stableDurationMs;
    private double toleranceMin;
    private double toleranceMax;
    private String captureMode;
    private boolean completionPhotoRequired;
}
