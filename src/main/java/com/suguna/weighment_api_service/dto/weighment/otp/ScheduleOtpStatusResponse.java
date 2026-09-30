package com.suguna.weighment_api_service.dto.weighment.otp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleOtpStatusResponse {

    private boolean success;
    private String scheduleId;
    private PartyOtpStatusDto farmer;
    private PartyOtpStatusDto trader;
}
