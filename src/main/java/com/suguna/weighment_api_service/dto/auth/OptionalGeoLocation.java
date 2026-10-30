package com.suguna.weighment_api_service.dto.auth;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Optional GPS fix from the mobile client (login / logout).
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OptionalGeoLocation {

    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double longitude;

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}
