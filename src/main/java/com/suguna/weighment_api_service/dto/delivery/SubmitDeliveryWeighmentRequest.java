package com.suguna.weighment_api_service.dto.delivery;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.AuditInfo;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.ScaleInfo;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentTotals;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
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
public class SubmitDeliveryWeighmentRequest {

    @NotBlank
    private String scheduleId;

    @NotBlank
    private String orderId;

    /** Expected value: DELIVERY */
    private String operationType;

    @NotBlank
    private String deviceId;

    private String supervisorId;

    private String saleType;

    private String birdType;

    @Valid
    private CustomerInfo customer;

    @Valid
    private ScaleInfo scale;

    @Valid
    @NotNull
    private ShopValidation shopValidation;

    private Double plannedQuantity;

    @Valid
    @NotEmpty
    @JsonProperty("weighmentEntries")
    @JsonAlias("rows")
    private List<WeighmentEntry> weighmentEntries;

    @Valid
    @NotNull
    private WeighmentTotals totals;

    @Valid
    private CustomerAcknowledgementMeta customerAcknowledgement;

    @Valid
    private PaymentInfo payment;

    @Valid
    private ShopStockInfo shopStock;

    @Valid
    private TimelineInfo timeline;

    @Valid
    private AuditInfo audit;

    public String effectiveSaleType() {
        return saleType == null ? null : saleType.trim();
    }

    public String effectiveBirdType() {
        return birdType == null ? null : birdType.trim();
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class CustomerInfo {
        @NotBlank
        private String customerCode;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class ShopValidation {
        private String verificationMethod;
        private String otpOrQrPayload;
        private Boolean verified;
        private Double latitude;
        private Double longitude;
        private Double geofenceRadiusMeters;
        private Boolean withinGeofence;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class CustomerAcknowledgementMeta {
        private Instant signedAt;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class PaymentInfo {
        private String collectionType;
        private String invoiceNo;
        private String dcNumber;
        private Double amountReceived;
        private String paymentMode;
        private String remarks;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class ShopStockInfo {
        private Double openingStockKg;
        private Double closingStockKg;
        private Integer openingBirds;
        private Integer closingBirds;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class TimelineInfo {
        private Instant arrivedAt;
        private Instant weighmentStartedAt;
        private Instant weighmentCompletedAt;
    }
}
