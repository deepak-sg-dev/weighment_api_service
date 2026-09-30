package com.suguna.weighment_api_service.dto.weighment.otp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyPartyOtpResponse {

    private boolean success;
    private String scheduleId;
    private PartyType partyType;
    private String message;
}
