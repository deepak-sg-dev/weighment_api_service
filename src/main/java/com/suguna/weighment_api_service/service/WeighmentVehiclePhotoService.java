package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse.VehiclePhotoInfo;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.storage.WeighmentImageDomain;
import com.suguna.weighment_api_service.storage.WeighmentImageKind;
import com.suguna.weighment_api_service.storage.WeighmentImageStorageService;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WeighmentVehiclePhotoService {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final WeighmentImageStorageService imageStorageService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleRetailEnrichmentService retailEnrichmentService;

    public WeighmentVehiclePhotoService(
            WeighmentImageStorageService imageStorageService,
            ScheduleRepository scheduleRepository,
            ScheduleRetailEnrichmentService retailEnrichmentService) {
        this.imageStorageService = imageStorageService;
        this.scheduleRepository = scheduleRepository;
        this.retailEnrichmentService = retailEnrichmentService;
    }

    public VehiclePhotoUploadResponse uploadVehiclePhotos(
            AuthenticatedSupervisor supervisor, String scheduleId, List<MultipartFile> photos, Instant capturedAt) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        LiftingSchedule schedule = scheduleRepository
                .findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Schedule not found: " + normalizedScheduleId));

        String saleType = retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
        var stored = imageStorageService.storeScheduleImages(
                photos, WeighmentImageDomain.LIFTING, saleType, normalizedScheduleId, WeighmentImageKind.VEHICLE, null);

        String uploadedAt = formatInstant(Instant.now());
        List<VehiclePhotoInfo> photoInfos = stored.stream()
                .map(item -> VehiclePhotoInfo.builder()
                        .fileKey(item.relativeFileKey())
                        .uploadedAt(uploadedAt)
                        .build())
                .toList();

        return VehiclePhotoUploadResponse.builder()
                .success(true)
                .scheduleId(normalizedScheduleId)
                .photo(photoInfos.isEmpty() ? null : photoInfos.get(0))
                .photos(photoInfos)
                .build();
    }

    private static String formatInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
    }
}
