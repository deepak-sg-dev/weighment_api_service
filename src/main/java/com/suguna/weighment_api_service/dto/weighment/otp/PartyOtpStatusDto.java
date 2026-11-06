package com.suguna.weighment_api_service.dto.weighment.otp;


import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PartyOtpStatusDto {

    private boolean required;
    private String mobile;
}