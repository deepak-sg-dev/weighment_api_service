package com.suguna.weighment_api_service.dto.weighment.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ResendPartyOtpRequest {

    @NotBlank
    @Size(max = 64)
    private String scheduleId;

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotNull
    private PartyType partyType;

    /** Required when {@link #partyType} is {@link PartyType#CUSTOMER}. */
    private String deliveryId;
}
