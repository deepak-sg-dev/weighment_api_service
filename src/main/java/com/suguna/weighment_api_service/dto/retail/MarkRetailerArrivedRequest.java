package com.suguna.weighment_api_service.dto.retail;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkRetailerArrivedRequest {

    @NotBlank
    private String retailerId;

    @NotNull
    private Instant arrivedAt;

    @Valid
    @NotNull
    private GpsPoint location;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpsPoint {
        private double latitude;
        private double longitude;
    }
}
