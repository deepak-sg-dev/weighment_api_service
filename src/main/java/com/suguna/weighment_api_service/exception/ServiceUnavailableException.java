package com.suguna.weighment_api_service.exception;

import com.suguna.weighment_api_service.constants.ErrorCode;

public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(ErrorCode.SERVICE_UNAVAILABLE, message);
    }

    public ServiceUnavailableException() {
        super(ErrorCode.ERP_UNAVAILABLE);
    }
}
