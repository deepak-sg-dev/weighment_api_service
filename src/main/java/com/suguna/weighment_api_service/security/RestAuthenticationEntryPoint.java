package com.suguna.weighment_api_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.dto.common.CommonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = createObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        String message = resolveMessage(request);
        CommonApiResponse<Void> body = CommonApiResponse.<Void>builder()
                .success(false)
                .errorCode(ErrorCode.UNAUTHORIZED.getErrorCode())
                .message(message)
                .details(java.util.Map.of())
                .timestamp(Instant.now())
                .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private static String resolveMessage(HttpServletRequest request) {
        Object failure = request.getAttribute(JwtAuthenticationFilter.REQUEST_AUTH_FAILURE);
        if (JwtAuthenticationFilter.FAILURE_INVALID.equals(failure)) {
            return "Invalid or expired access token";
        }
        if (JwtAuthenticationFilter.FAILURE_MALFORMED.equals(failure)) {
            return "Authorization must be sent as: Bearer <accessToken from device-login>";
        }
        if (JwtAuthenticationFilter.FAILURE_MISSING.equals(failure)) {
            return "Missing access token. Send header Authorization: Bearer <accessToken>";
        }
        return ErrorCode.UNAUTHORIZED.getDefaultMessage();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
