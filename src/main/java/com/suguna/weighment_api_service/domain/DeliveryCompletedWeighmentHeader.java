package com.suguna.weighment_api_service.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Payload to insert one {@code sug_mai_birds_weighment} row when a retail shop delivery completes. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCompletedWeighmentHeader {

    private String scheduleId;
    private String supervisorCode;
    private String retailOrderNo;
    private String customerCode;
    private String customerName;
    private Double orderedQty;
    private Integer noOfBirds;
    private Integer noOfCages;
    private Double emptyWt;
    private Double grossWt;
    private Double netWt;
    private Double avgWt;
    private Double latitude;
    private Double longitude;
    private Instant wmStartTime;
    private Instant wmEndTime;
    private String manualDcNo;
}
