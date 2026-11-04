package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.weighment.otp.ResendPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.ResendPartyOtpResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.ScheduleOtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weighment/otp")
@Tag(name = "Weighment OTP (V2)", description = "Resend party OTP for lifting and retail delivery")
public class WeighmentOtpController {

    private final ScheduleOtpService scheduleOtpService;

    public WeighmentOtpController(ScheduleOtpService scheduleOtpService) {
        this.scheduleOtpService = scheduleOtpService;
    }

    @PostMapping("/resend")
    public ResponseEntity<ResendPartyOtpResponse> resendPartyOtp(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @Valid @RequestBody ResendPartyOtpRequest request) {
        return ResponseEntity.ok(scheduleOtpService.resendPartyOtp(supervisor, request));
    }
}
