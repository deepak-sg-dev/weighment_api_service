package com.suguna.weighment_api_service.dto.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCompletedWeighmentRequest {

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String localTransactionId;

    @NotBlank
    private String scheduleId;

    @NotBlank
    private String deviceId;

    private String supervisorId;

    private String operationType;

    @Valid
    private ScaleInfo scale;

    @Valid
    @NotNull
    private FarmValidation farmValidation;

    @Valid
    private VehicleInfo vehicle;

    private Integer plannedQuantity;

    @Valid
    private WeighmentSetup weighmentSetup;

    @Valid
    @NotEmpty
    private List<WeighmentRow> rows;

    @Valid
    @NotNull
    private WeighmentTotals totals;

    @Valid
    private CompletionInfo completion;

    @Valid
    private AuditInfo audit;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScaleInfo {
        private String scaleType;
        private String deviceId;
        private String pcbId;
        private String firmwareVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmValidation {
        private Double farmLatitude;
        private Double farmLongitude;
        private Integer configuredGeofenceRadiusMeters;
        private Double capturedLatitude;
        private Double capturedLongitude;
        private Double accuracyMeters;
        private Double distanceFromFarmMeters;
        private Boolean geofencePassed;
        private Boolean validatedLocally;
        private Instant validatedAt;
        private Boolean farmerOtpVerified;
        private Boolean traderOtpVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private String registrationNumber;
        private List<String> evidenceReferences;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeighmentSetup {
        private Double averageBirdWeight;
        private Integer standardBirdsPerCage;
        private Integer stableDurationMs;
        private Double toleranceMin;
        private Double toleranceMax;
        private String captureMode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeighmentRow {
        private Integer sequence;
        private String cageOrSetReference;
        private Integer birdsApplied;
        private Boolean birdsAdjusted;
        private String adjustmentReason;
        private Double tare;
        private Double gross;
        private Double net;
        private Double averageBirdWeight;
        private String toleranceStatus;
        private Instant capturedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeighmentTotals {
        private Integer totalCagesOrBatches;
        private Integer totalBirds;
        private Double totalTare;
        private Double totalGross;
        private Double totalNet;
        private Double overallAverageBirdWeight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionInfo {
        private Integer claimedBirds;
        private Integer replacementBirds;
        private Integer lameBirds;
        private Integer deadBirds;
        private Integer rejectedBirds;
        private Integer returnedBirds;
        private Instant weighingStartTime;
        private Instant outTime;
        private String remarks;
        private List<PartyAcknowledgement> acknowledgements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyAcknowledgement {
        private String party;
        private String reference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditInfo {
        private List<Object> abortedRows;
        private List<Object> stabilityErrors;
        private List<Object> toleranceAlerts;
        private List<Object> overrides;
    }
}
