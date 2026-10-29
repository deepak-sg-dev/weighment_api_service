package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.common.WeighmentImageUploadResponse;
import com.suguna.weighment_api_service.dto.common.WeighmentImageUploadResponse.UploadedImage;
import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse.VehiclePhotoInfo;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.repository.DeliveryWeighmentRepository;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.storage.WeighmentImageCategory;
import com.suguna.weighment_api_service.storage.WeighmentImageDomain;
import com.suguna.weighment_api_service.storage.WeighmentImageStorageService;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WeighmentImageUploadService {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final WeighmentImageStorageService imageStorageService;
    private final ScheduleRepository scheduleRepository;
    private final RetailDeliveryRepository retailDeliveryRepository;
    private final DeliveryWeighmentRepository deliveryWeighmentRepository;
    private final ScheduleRetailEnrichmentService retailEnrichmentService;

    public WeighmentImageUploadService(
            WeighmentImageStorageService imageStorageService,
            ScheduleRepository scheduleRepository,
            RetailDeliveryRepository retailDeliveryRepository,
            DeliveryWeighmentRepository deliveryWeighmentRepository,
            ScheduleRetailEnrichmentService retailEnrichmentService) {
        this.imageStorageService = imageStorageService;
        this.scheduleRepository = scheduleRepository;
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.deliveryWeighmentRepository = deliveryWeighmentRepository;
        this.retailEnrichmentService = retailEnrichmentService;
    }

    @Transactional
    public WeighmentImageUploadResponse upload(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            WeighmentImageCategory category,
            String orderId,
            WeighmentImageDomain domainOverride,
            List<MultipartFile> photos,
            Instant capturedAt,
            boolean linkDeliveryReceipt) {
        if (photos == null || photos.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "At least one photo is required");
        }
        if (category.isOrderIdRequired() && (orderId == null || orderId.isBlank())) {
            throw new BadRequestException("orderId is required for category " + category.name());
        }

        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        WeighmentImageDomain domain = resolveDomain(category, domainOverride);
        RetailScheduleContext retailSchedule = ensureScheduleAccess(supervisor, normalizedScheduleId, domain);
        String saleType = resolveSaleType(supervisor, normalizedScheduleId, domain);
        String refId = orderId == null || orderId.isBlank() ? null : orderId.trim();

        if (linkDeliveryReceipt && domain == WeighmentImageDomain.DELIVERY && refId != null && retailSchedule != null) {
            deliveryWeighmentRepository.assertReadyForDeliveryEvidence(retailSchedule, refId);
        }

        var stored = imageStorageService.storeScheduleImages(
                photos, domain, saleType, normalizedScheduleId, category.kind(), refId);

        if (linkDeliveryReceipt && domain == WeighmentImageDomain.DELIVERY && refId != null && retailSchedule != null) {
            linkDeliveryReceipt(retailSchedule, category, refId, stored.get(0).relativeFileKey());
        }

        String uploadedAt = formatInstant(Instant.now());
        List<UploadedImage> uploaded = stored.stream()
                .map(item -> UploadedImage.builder()
                        .fileKey(item.relativeFileKey())
                        .uploadedAt(uploadedAt)
                        .build())
                .toList();

        return WeighmentImageUploadResponse.builder()
                .success(true)
                .scheduleId(normalizedScheduleId)
                .orderId(refId)
                .category(category.name())
                .domain(domain.folderName())
                .capturedAt(capturedAt != null ? formatInstant(capturedAt) : null)
                .photo(uploaded.isEmpty() ? null : uploaded.get(0))
                .photos(uploaded)
                .build();
    }

    public VehiclePhotoUploadResponse toVehiclePhotoResponse(WeighmentImageUploadResponse source) {
        if (source.getPhotos() == null) {
            return VehiclePhotoUploadResponse.builder()
                    .success(source.isSuccess())
                    .scheduleId(source.getScheduleId())
                    .build();
        }
        List<VehiclePhotoInfo> infos = source.getPhotos().stream()
                .map(item -> VehiclePhotoInfo.builder()
                        .fileKey(item.getFileKey())
                        .uploadedAt(item.getUploadedAt())
                        .build())
                .toList();
        return VehiclePhotoUploadResponse.builder()
                .success(source.isSuccess())
                .scheduleId(source.getScheduleId())
                .photos(infos)
                .photo(infos.isEmpty() ? null : infos.get(0))
                .build();
    }

    public static WeighmentImageDomain parseDomainOverride(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("LIFTING".equals(normalized)) {
            return WeighmentImageDomain.LIFTING;
        }
        if ("DELIVERY".equals(normalized)) {
            return WeighmentImageDomain.DELIVERY;
        }
        throw new ApiException(ErrorCode.BAD_REQUEST, "domain must be LIFTING or DELIVERY");
    }

    private WeighmentImageDomain resolveDomain(
            WeighmentImageCategory category, WeighmentImageDomain domainOverride) {
        if (category == WeighmentImageCategory.OTHER && domainOverride != null) {
            return domainOverride;
        }
        return category.defaultDomain();
    }

    private RetailScheduleContext ensureScheduleAccess(
            AuthenticatedSupervisor supervisor, String normalizedScheduleId, WeighmentImageDomain domain) {
        if (domain == WeighmentImageDomain.DELIVERY) {
            return retailDeliveryRepository.loadRetailSchedule(
                    supervisor.getSupervisor().getCode(), normalizedScheduleId);
        }
        scheduleRepository
                .findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Schedule not found: " + normalizedScheduleId));
        return null;
    }

    private String resolveSaleType(
            AuthenticatedSupervisor supervisor, String normalizedScheduleId, WeighmentImageDomain domain) {
        if (domain == WeighmentImageDomain.DELIVERY) {
            return "RETAIL";
        }
        LiftingSchedule schedule = scheduleRepository
                .findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Schedule not found: " + normalizedScheduleId));
        return retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
    }

    private void linkDeliveryReceipt(
            RetailScheduleContext schedule,
            WeighmentImageCategory category,
            String orderId,
            String fileKey) {
        switch (category) {
            case SHOP -> deliveryWeighmentRepository.recordShopPhoto(schedule, orderId, fileKey);
            case SIGNATURE -> deliveryWeighmentRepository.recordSignaturePhoto(schedule, orderId, fileKey);
            case CUSTOMER -> deliveryWeighmentRepository.recordCustomerPhoto(schedule, orderId, fileKey);
            default -> {}
        }
    }

    private static String formatInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
    }
}
