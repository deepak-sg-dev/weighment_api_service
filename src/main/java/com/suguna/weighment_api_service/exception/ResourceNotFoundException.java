package com.suguna.weighment_api_service.exception;

import com.suguna.weighment_api_service.constants.ErrorCode;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ErrorCode.NOT_FOUND, "%s not found with id: %s".formatted(resourceName, identifier));
    }
}
