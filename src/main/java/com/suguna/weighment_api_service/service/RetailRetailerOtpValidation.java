package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpResolution;

public final class RetailRetailerOtpValidation {

    private RetailRetailerOtpValidation() {}

    public static void validate(
            OracleErpProperties erpProperties, RetailRetailerOtpResolution resolution, String retailerOtp) {
        if (!erpProperties.isRetailOtpValidationEnabled()) {
            return;
        }
        if (!resolution.configured()) {
            return;
        }
        if (resolution.expired()) {
            throw new BusinessValidationException(ErrorCodes.OTP_EXPIRED, "Retailer OTP has expired");
        }
        if (retailerOtp == null || !resolution.expectedCode().equals(retailerOtp.trim())) {
            throw new BusinessValidationException(ErrorCodes.INVALID_RETAILER_OTP, "Invalid retailer OTP");
        }
    }
}
