package com.suguna.weighment_api_service.dto.weighment.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateScheduleVehicleRequest {

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotNull
    @Valid
    private VehiclePayload vehicle;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehiclePayload {

        @NotBlank
        @Pattern(regexp = "^[A-Z0-9]{6,13}$", message = "vehicle number must be uppercase alphanumeric without spaces")
        private String number;

        @NotBlank
        @Size(min = 2, max = 60)
        @Pattern(regexp = "^[A-Za-z\\s.'-]+$", message = "driver name must be letters and spaces")
        private String driverName;

        @NotBlank
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "driver mobile must be 10 digits starting with 6-9")
        private String driverMobile;
    }
}
