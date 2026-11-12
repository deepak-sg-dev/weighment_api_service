package com.suguna.weighment_api_service.dto.retail;

import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
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
public class RetailDeliveryOrderSummaryDto {

    private String retailOrderId;
    private String retailerId;
    private String retailerName;
    private String salesOrderNo;
    private double orderedQuantity;
    private int orderedBirdCount;
    /** Delivered kg from DTL {@code message} ({@code WT} / weighment payload), not DTL {@code quantity}. */
    private Double deliveredQuantityKg;
    private Integer deliveredBirdCount;
    /** Same as {@link #deliveryStatus}; kept for backward compatibility. */
    private String status;
    /** Per-order delivery: PENDING, IN_PROGRESS, COMPLETED. */
    private String deliveryStatus;
    /** ISO-8601 UTC when delivery was started for this order; null if not started. */
    private String deliveryStartedAt;
    /** Shop address (schedule enrichment / optional on retail list). */
    private String retailerAddress;
    /** Shop GSTIN from {@code sug_gst_pkg.get_reg_no('GSTN','CS', cust_acct_site_id, SYSDATE)}. */
    private String gstNo;
    private Double retailerLatitude;
    private Double retailerLongitude;
    /** Vehicle for this shop delivery leg (may differ from schedule lifting vehicle). */
    private ScheduleVehicleDto deliveryVehicle;

    /** Shop OTP from DTL / message (schedule detail only; not used on list). */
    private String retailOtp;
    private java.time.Instant retailOtpExpiresAt;
    private String message;
}