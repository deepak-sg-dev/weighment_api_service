package com.suguna.weighment_api_service.dto.weighment.otp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartyOtpStatusDto {

    private boolean required;
    private String mobile;
}
