package com.suguna.weighment_api_service.exception;

import com.suguna.weighment_api_service.constants.ErrorCode;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
