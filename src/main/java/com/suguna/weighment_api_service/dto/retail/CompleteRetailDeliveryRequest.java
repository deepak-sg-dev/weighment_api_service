package com.suguna.weighment_api_service.dto.retail;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CompleteRetailDeliveryRequest {

    @NotBlank
    private String idempotencyKey;

    @NotBlank
    private String scheduleId;

    @NotBlank
    private String retailerId;

    @NotBlank
    private String retailerOtp;

    @Valid
    @NotNull
    private DeliveryLocation deliveryLocation;

    @Valid
    @NotNull
    private ShopImage shopImage;

    private String scaleDeviceId;
    private double deliveredWeight;
    private int deliveredBirdCount;
    private Double averageBirdWeight;
    private int lameBirds;
    private int replacementBirds;

    @NotNull
    private Instant completedAt;

    private String remarks;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class DeliveryLocation {
        private double latitude;
        private double longitude;
        private Double accuracyMeters;
        private Instant capturedAt;
    }

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class ShopImage {
        private String fileName;
        private String mimeType;
        private String reference;
    }
}