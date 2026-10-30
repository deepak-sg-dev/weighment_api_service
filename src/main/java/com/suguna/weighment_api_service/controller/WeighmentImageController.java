package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.common.WeighmentImageUploadResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.WeighmentImageUploadService;
import com.suguna.weighment_api_service.storage.WeighmentImageCategory;
import com.suguna.weighment_api_service.util.MultipartPhotos;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/weighment/images")
@Tag(
        name = "Weighment Images",
        description =
                "Common multipart upload by category (VEHICLE, SHOP, SIGNATURE, CUSTOMER, OTHER). "
                        + "Delivery categories require orderId; OTHER accepts optional domain=LIFTING|DELIVERY.")
public class WeighmentImageController {

    private final WeighmentImageUploadService imageUploadService;

    public WeighmentImageController(WeighmentImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WeighmentImageUploadResponse> uploadImages(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId,
            @RequestParam String category,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String domain,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant capturedAt) {
        WeighmentImageCategory parsedCategory = WeighmentImageCategory.parse(category);
        return ResponseEntity.ok(imageUploadService.upload(
                supervisor,
                scheduleId,
                parsedCategory,
                orderId,
                WeighmentImageUploadService.parseDomainOverride(domain),
                MultipartPhotos.resolve(photo, photos),
                capturedAt,
                true));
    }
}
