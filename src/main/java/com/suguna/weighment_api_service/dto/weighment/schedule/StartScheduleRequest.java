package com.suguna.weighment_api_service.dto.weighment.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class StartScheduleRequest {

    /** Optional until LOCAL_TRANSACTION_ID exists on lifting order; not persisted today. */
    @Size(max = 128)
    private String localTransactionId;

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotNull
    private Instant startedAt;

    @Size(max = 32)
    private String vehicleRegistration;

    @NotNull
    private GeoPointDto location;

    public StartScheduleRequest() {
    }

    public String getLocalTransactionId() {
        return localTransactionId;
    }

    public void setLocalTransactionId(String localTransactionId) {
        this.localTransactionId = localTransactionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public GeoPointDto getLocation() {
        return location;
    }

    public void setLocation(GeoPointDto location) {
        this.location = location;
    }
}
