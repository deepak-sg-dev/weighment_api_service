package com.suguna.weighment_api_service.dto.weighment.otp;

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
public class ResendPartyOtpResponse {

    private boolean success;
    private String message;
    private String scheduleId;
    private PartyType partyType;
    private String sentAt;
    private int expiresInSeconds;
    private int resendCount;
    private int maxResends;
    private int nextResendAfterSeconds;
}
