package com.suguna.weighment_api_service.dto.weighment.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPartyOtpRequest {

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotNull
    private PartyType partyType;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4,8}$", message = "otp must be 4 to 8 digits")
    private String otp;
}
