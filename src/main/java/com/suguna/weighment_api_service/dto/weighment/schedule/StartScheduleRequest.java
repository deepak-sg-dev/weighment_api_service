package com.suguna.weighment_api_service.dto.weighment.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class StartScheduleRequest {

    @NotBlank
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
}
