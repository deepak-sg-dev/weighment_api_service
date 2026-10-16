package com.suguna.weighment_api_service.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceLoginRequest {

    @NotBlank(message = "deviceId is required")
    @Size(max = 128, message = "deviceId must be at most 128 characters")
    private String deviceId;

    @NotBlank(message = "appVersion is required")
    @Size(max = 32)
    private String appVersion;

    @NotBlank(message = "platform is required")
    @Size(max = 32)
    private String platform;

    @Size(max = 128)
    private String deviceName;

    /** Device make / brand (e.g. Samsung, Apple). Optional. */
    @JsonAlias({"manufacturer", "mobileManufacturer", "deviceManufacturer"})
    @Size(max = 128)
    private String deviceManufacturer;

    /** OS release from Android or iOS (e.g. 14, 17.4.1). Optional; {@link #platform} remains ANDROID / IOS. */
    @Size(max = 64)
    private String osVersion;

    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double latitude;

    @JsonAlias("lng")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double longitude;

    /** Nested {@code location: { latitude, longitude }} (optional). */
    @Valid
    private OptionalGeoLocation location;

    @AssertTrue(message = "latitude and longitude must both be provided when either is set")
    public boolean isLocationPairValid() {
        if (!locationPairValid(latitude, longitude)) {
            return false;
        }
        if (location == null) {
            return true;
        }
        return locationPairValid(location.getLatitude(), location.getLongitude());
    }

    private static boolean locationPairValid(Double lat, Double lon) {
        if (lat == null && lon == null) {
            return true;
        }
        return lat != null && lon != null;
    }

    public Double resolvedLatitude() {
        if (latitude != null) {
            return latitude;
        }
        return location == null ? null : location.getLatitude();
    }

    public Double resolvedLongitude() {
        if (longitude != null) {
            return longitude;
        }
        return location == null ? null : location.getLongitude();
    }
}
