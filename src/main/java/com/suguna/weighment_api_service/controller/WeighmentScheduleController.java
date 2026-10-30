package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.weighment.otp.ScheduleOtpStatusResponse;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleActionResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.ScheduleOtpService;
import com.suguna.weighment_api_service.service.ScheduleService;
import com.suguna.weighment_api_service.service.WeighmentVehiclePhotoService;
import com.suguna.weighment_api_service.util.MultipartPhotos;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/weighment/schedules")
@Tag(name = "Lifting Schedules", description = "Schedule list, details, download and start")
public class WeighmentScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleOtpService scheduleOtpService;
    private final WeighmentVehiclePhotoService vehiclePhotoService;

    public WeighmentScheduleController(
            ScheduleService scheduleService,
            ScheduleOtpService scheduleOtpService,
            WeighmentVehiclePhotoService vehiclePhotoService) {
        this.scheduleService = scheduleService;
        this.scheduleOtpService = scheduleOtpService;
        this.vehiclePhotoService = vehiclePhotoService;
    }

    @GetMapping
    public ResponseEntity<ScheduleListResponse> listSchedules(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer lookbackDays,
            @RequestParam(required = false) String scheduleIds) {
        return ResponseEntity.ok(
                scheduleService.getAssignedSchedules(supervisor, status, lookbackDays, scheduleIds));
    }

    /** Use for composite schedule ids (e.g. {@code 211342644/1.1}) — slashes cannot be extra path segments. */
    @GetMapping("/detail")
    public ResponseEntity<ScheduleDetailResponse> getScheduleDetail(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleDetails(supervisor, scheduleId));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getSchedule(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId) {
        return ResponseEntity.ok(scheduleService.getScheduleDetails(supervisor, scheduleId));
    }

    @PostMapping("/download")
    public ResponseEntity<ScheduleActionResponse> markDownloadedByQuery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId,
            @Valid @RequestBody MarkScheduleDownloadedRequest request) {
        return ResponseEntity.ok(scheduleService.markDownloaded(supervisor, scheduleId, request));
    }

    @PostMapping("/start")
    public ResponseEntity<ScheduleActionResponse> startScheduleByQuery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId,
            @Valid @RequestBody StartScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.startSchedule(supervisor, scheduleId, request));
    }

    @GetMapping("/otp")
    public ResponseEntity<ScheduleOtpStatusResponse> getOtpStatusByQuery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId) {
        return ResponseEntity.ok(scheduleOtpService.getOtpStatus(supervisor, scheduleId));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<VerifyPartyOtpResponse> verifyPartyOtpByQuery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId,
            @Valid @RequestBody VerifyPartyOtpRequest request) {
        return ResponseEntity.ok(scheduleOtpService.verifyPartyOtp(supervisor, scheduleId, request));
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

    @PostMapping(value = "/vehicle-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehiclePhotoUploadResponse> uploadVehiclePhotoByQuery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant capturedAt) {
        return ResponseEntity.ok(vehiclePhotoService.uploadVehiclePhotos(
                supervisor, scheduleId, MultipartPhotos.resolve(photo, photos), capturedAt));
    }

    @PostMapping(value = "/{scheduleId}/vehicle-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehiclePhotoUploadResponse> uploadVehiclePhoto(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String scheduleId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant capturedAt) {
        return ResponseEntity.ok(vehiclePhotoService.uploadVehiclePhotos(
                supervisor, scheduleId, MultipartPhotos.resolve(photo, photos), capturedAt));
    }
}
