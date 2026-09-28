package com.suguna.weighment_api_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.dto.common.CommonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        CommonApiResponse<Void> body = CommonApiResponse.<Void>builder()
                .success(false)
                .errorCode(ErrorCode.UNAUTHORIZED.getErrorCode())
                .message(ErrorCode.UNAUTHORIZED.getDefaultMessage())
                .details(java.util.Map.of())
                .timestamp(Instant.now())
                .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
