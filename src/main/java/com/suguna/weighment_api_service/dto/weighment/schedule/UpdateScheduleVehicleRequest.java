package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.suguna.weighment_api_service.util.VehicleRegistrationSupport;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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
    public static class VehiclePayload {

        /**
         * Stored normalized. ERP default {@code -} and alternate JSON keys ({@code vehicleNo}) are
         * merged so a real plate is not discarded.
         */
        @Setter(AccessLevel.NONE)
        @NotBlank(message = "vehicle number is required")
        @Pattern(
                regexp = "^[A-Z0-9]{6,13}$",
                message = "vehicle number must be 6–13 letters/digits (hyphens and spaces are stripped)")
        private String number;

        @NotBlank
        @Size(min = 2, max = 60)
        @Pattern(regexp = "^[A-Za-z\\s.'-]+$", message = "driver name must be letters and spaces")
        private String driverName;

        @NotBlank
        @Pattern(regexp = "^[6-9][0-9]{9}$", message = "driver mobile must be 10 digits starting with 6-9")
        private String driverMobile;

        @JsonGetter("number")
        public String getNumber() {
            return number;
        }

        @JsonSetter("number")
        public void setNumber(String number) {
            this.number = VehicleRegistrationSupport.merge(this.number, number);
        }

        @JsonSetter("vehicleNo")
        public void setVehicleNo(String vehicleNo) {
            this.number = VehicleRegistrationSupport.merge(this.number, vehicleNo);
        }

        @JsonSetter("vehicleNumber")
        public void setVehicleNumber(String vehicleNumber) {
            setVehicleNo(vehicleNumber);
        }

        @JsonSetter("registrationNumber")
        public void setRegistrationNumber(String registrationNumber) {
            setVehicleNo(registrationNumber);
        }
    }
}
