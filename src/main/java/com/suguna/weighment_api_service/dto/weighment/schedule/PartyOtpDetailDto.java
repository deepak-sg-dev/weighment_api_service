package com.suguna.weighment_api_service.dto.weighment.schedule;

/** Farmer / trader OTP context on schedule detail (from lifting order). */
public class PartyOtpDetailDto {

    private boolean required;
    private String mobile;
    private String otp;

    public PartyOtpDetailDto() {}

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
