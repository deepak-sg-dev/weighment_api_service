package com.suguna.weighment_api_service.dto.delivery;

import com.suguna.weighment_api_service.dto.weighment.schedule.GeoPointDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StartRetailDeliveryOrderRequest {

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotBlank
    @Size(max = 32)
    private String retailerId;

    @NotNull
    private Instant startedAt;

    @Valid
    @NotNull
    private GeoPointDto location;
}
