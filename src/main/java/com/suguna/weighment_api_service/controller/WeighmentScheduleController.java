package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.weighment.otp.ScheduleOtpStatusResponse;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleActionResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.ScheduleOtpService;
import com.suguna.weighment_api_service.service.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weighment/schedules")
@Tag(name = "Lifting Schedules", description = "Schedule list, details, download and start")
public class WeighmentScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleOtpService scheduleOtpService;

    public WeighmentScheduleController(ScheduleService scheduleService, ScheduleOtpService scheduleOtpService) {
        this.scheduleService = scheduleService;
        this.scheduleOtpService = scheduleOtpService;
    }

    @GetMapping
    public ResponseEntity<ScheduleListResponse> listSchedules(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(scheduleService.getAssignedSchedules(supervisor, status));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getSchedule(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleDetails(supervisor, scheduleId));
    }

    @PostMapping("/{scheduleId}/download")
    public ResponseEntity<ScheduleActionResponse> markDownloaded(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId,
            @Valid @RequestBody MarkScheduleDownloadedRequest request) {
        return ResponseEntity.ok(scheduleService.markDownloaded(supervisor, scheduleId, request));
    }

    @PostMapping("/{scheduleId}/start")
    public ResponseEntity<ScheduleActionResponse> startSchedule(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId,
            @Valid @RequestBody StartScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.startSchedule(supervisor, scheduleId, request));
    }

    @GetMapping("/{scheduleId}/otp")
    public ResponseEntity<ScheduleOtpStatusResponse> getOtpStatus(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor, @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleOtpService.getOtpStatus(supervisor, scheduleId));
    }

    @PostMapping("/{scheduleId}/otp/verify")
    public ResponseEntity<VerifyPartyOtpResponse> verifyPartyOtp(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId,
            @Valid @RequestBody VerifyPartyOtpRequest request) {
        return ResponseEntity.ok(scheduleOtpService.verifyPartyOtp(supervisor, scheduleId, request));
    }
}
