package com.suguna.weighment_api_service.dto.transaction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCompletedWeighmentRequest {

    @NotBlank
    private String scheduleId;

    @NotBlank
    private String deviceId;

    private String supervisorId;

    private String saleType;

    private String birdType;

    /** @deprecated Use {@link #birdType}; accepted for older clients. */
    @Deprecated
    @JsonAlias("operationType")
    private String operationType;

    @Valid
    private ScaleInfo scale;

    @Valid
    @NotNull
    private FarmValidation farmValidation;

    @Valid
    private VehicleInfo vehicle;

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
    private CompletionInfo completion;

    @Valid
    private AuditInfo audit;

    public String effectiveBirdType() {
        if (birdType != null && !birdType.isBlank()) {
            return birdType.trim();
        }
        if (operationType != null && !operationType.isBlank()) {
            return operationType.trim();
        }
        return null;
    }

    public String effectiveSaleType() {
        return saleType == null ? null : saleType.trim();
    }

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
        private Boolean geofencePassed;
        private Boolean validatedLocally;
        private Instant validatedAt;
        private Boolean farmerOtpVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private String registrationNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeighmentEntry {
        private Integer sequence;
        private String category;
        private Integer birdsApplied;
        private Double gross;
        private Double net;
        private Double averageBirdWeight;
        private Double weightDifference;
        private String status;
        private Instant startTime;
        private Instant endTime;
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
