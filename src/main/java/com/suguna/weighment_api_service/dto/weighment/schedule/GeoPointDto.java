package com.suguna.weighment_api_service.dto.weighment.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoPointDto {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
