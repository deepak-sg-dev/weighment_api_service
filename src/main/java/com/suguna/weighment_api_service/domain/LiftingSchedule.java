package com.suguna.weighment_api_service.domain;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import java.time.Instant;
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
public class LiftingSchedule {

    private String scheduleId;
    private String supervisorId;
    private String orderSource;
    private Instant scheduledAt;
    private double plannedQuantityKg;
    private double declaredBodyWeightKg;
    private String branchCode;
    private String branchId;
    private Long salesOrderNo;
    private String vehicleNumber;
    private String farmLocation;
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
    /** From {@code lifting_status} on {@code sug_mai_birds_lifting_order}. */
    private String liftingStatus;
    /** From {@code delivery_status}; nullable — API may fall back for retail. */
    private String deliveryStatus;
    private Instant startedAt;
    private int standardBirdsPerCage;
    private long stableDurationMs;
    private double toleranceMin;
    private double toleranceMax;
    private String captureMode;
    private boolean completionPhotoRequired;
}