package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.DeliveryCustomerAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.delivery.DeliveryShopPhotoResponse;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest.PaymentInfo;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.repository.DeliveryWeighmentRepository;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.storage.WeighmentImageCategory;
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
public class DeliveryWeighmentService {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final DeliveryWeighmentRepository deliveryWeighmentRepository;
    private final RetailDeliveryRepository retailDeliveryRepository;
    private final WeighmentImageUploadService imageUploadService;
    private final OracleErpProperties erpProperties;

    public DeliveryWeighmentService(
            DeliveryWeighmentRepository deliveryWeighmentRepository,
            RetailDeliveryRepository retailDeliveryRepository,
            WeighmentImageUploadService imageUploadService,
            OracleErpProperties erpProperties) {
        this.deliveryWeighmentRepository = deliveryWeighmentRepository;
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.imageUploadService = imageUploadService;
        this.erpProperties = erpProperties;
    }

    @Transactional
    public SubmitDeliveryWeighmentResponse submitDeliveryWeighment(
            AuthenticatedSupervisor supervisor, SubmitDeliveryWeighmentRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        validateSupervisor(supervisor, request.getSupervisorId());
        validateRetailSaleType(request);
        validateShopValidation(request);
        validatePayment(request.getPayment());

        RetailScheduleContext schedule = loadSchedule(supervisor, request.getScheduleId());
        validateRetailerOtp(schedule, request.getOrderId(), extractOtp(request));

        return deliveryWeighmentRepository.submitWeighment(schedule, request);
    }

    @Transactional
    public DeliveryShopPhotoResponse uploadShopPhoto(
            AuthenticatedSupervisor supervisor,
            String orderId,
            String scheduleId,
            MultipartFile photo,
            Instant capturedAt) {
        if (photo == null || photo.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo is required");
        }
        var uploaded = imageUploadService.upload(
                supervisor,
                scheduleId,
                WeighmentImageCategory.SHOP,
                orderId,
                null,
                List.of(photo),
                capturedAt,
                true);

        return DeliveryShopPhotoResponse.builder()
                .success(true)
                .orderId(orderId)
                .photo(DeliveryShopPhotoResponse.UploadedFile.builder()
                        .fileKey(uploaded.getPhoto().getFileKey())
                        .build())
                .uploadedAt(uploaded.getPhoto().getUploadedAt())
                .build();
    }

    @Transactional
    public DeliveryCustomerAcknowledgementResponse uploadCustomerAcknowledgement(
            AuthenticatedSupervisor supervisor,
            String orderId,
            String scheduleId,
            MultipartFile signature,
            MultipartFile customerPhoto,
            Instant signedAt) {
        if (signature == null || signature.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "signature is required");
        }
        if (customerPhoto == null || customerPhoto.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "customerPhoto is required");
        }
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);

        var signatureUploaded = imageUploadService.upload(
                supervisor,
                scheduleId,
                WeighmentImageCategory.SIGNATURE,
                orderId,
                null,
                List.of(signature),
                signedAt,
                false);
        var customerUploaded = imageUploadService.upload(
                supervisor,
                scheduleId,
                WeighmentImageCategory.CUSTOMER,
                orderId,
                null,
                List.of(customerPhoto),
                signedAt,
                false);

        String signatureKey = signatureUploaded.getPhoto().getFileKey();
        String customerKey = customerUploaded.getPhoto().getFileKey();
        deliveryWeighmentRepository.recordCustomerAcknowledgement(
                schedule, orderId, signatureKey, customerKey);

        return DeliveryCustomerAcknowledgementResponse.builder()
                .success(true)
                .orderId(orderId)
                .files(DeliveryCustomerAcknowledgementResponse.AcknowledgementFiles.builder()
                        .signature(signatureKey)
                        .customerPhoto(customerKey)
                        .build())
                .uploadedAt(formatInstant(signedAt != null ? signedAt : Instant.now()))
                .build();
    }

    private RetailScheduleContext loadSchedule(AuthenticatedSupervisor supervisor, String scheduleId) {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new BadRequestException("scheduleId is required");
        }
        return retailDeliveryRepository.loadRetailSchedule(
                supervisor.getSupervisor().getCode(), ScheduleIdSupport.normalize(scheduleId));
    }

    private void validateRetailSaleType(SubmitDeliveryWeighmentRequest request) {
        String saleType = request.effectiveSaleType();
        if (saleType != null && !saleType.isBlank() && !"RETAIL".equalsIgnoreCase(saleType)) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Delivery weighment is supported for RETAIL only");
        }
        String op = request.getOperationType();
        if (op != null && !op.isBlank() && !"DELIVERY".equalsIgnoreCase(op.trim())) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "operationType must be DELIVERY");
        }
    }

    private void validateShopValidation(SubmitDeliveryWeighmentRequest request) {
        var shop = request.getShopValidation();
        if (shop == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Shop validation evidence is required");
        }
        if (!Boolean.TRUE.equals(shop.getVerified())) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Shop OTP/QR verification must be completed before submission");
        }
        if (!Boolean.TRUE.equals(shop.getWithinGeofence())) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Geofence validation failed");
        }
    }

    private void validatePayment(PaymentInfo payment) {
        if (payment == null || payment.getCollectionType() == null) {
            return;
        }
        String type = payment.getCollectionType().trim().toUpperCase(Locale.ROOT);
        switch (type) {
            case "INVOICE" -> {
                requireNonBlank(payment.getInvoiceNo(), "payment.invoiceNo");
                requireAmount(payment);
            }
            case "DC" -> {
                requireNonBlank(payment.getDcNumber(), "payment.dcNumber");
                requireAmount(payment);
            }
            case "ADHOC" -> requireAmount(payment);
            default -> throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Unsupported payment.collectionType: " + type);
        }
    }

    private static void requireAmount(PaymentInfo payment) {
        if (payment.getAmountReceived() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "payment.amountReceived is required");
        }
        requireNonBlank(payment.getPaymentMode(), "payment.paymentMode");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, field + " is required");
        }
    }

    private void validateRetailerOtp(RetailScheduleContext schedule, String orderId, String retailerOtp) {
        if (!erpProperties.isRetailOtpValidationEnabled()) {
            return;
        }
        var expected = retailDeliveryRepository.findExpectedRetailerOtp(schedule, orderId);
        if (expected.isEmpty()) {
            return;
        }
        if (retailerOtp == null || !expected.get().equals(retailerOtp.trim())) {
            throw new BusinessValidationException(ErrorCodes.INVALID_RETAILER_OTP, "Invalid retailer OTP");
        }
    }

    private static String extractOtp(SubmitDeliveryWeighmentRequest request) {
        if (request.getShopValidation() == null) {
            return null;
        }
        String payload = request.getShopValidation().getOtpOrQrPayload();
        if (payload == null || payload.isBlank()) {
            return null;
        }
        String trimmed = payload.trim();
        if (trimmed.length() <= 8 && trimmed.chars().allMatch(Character::isDigit)) {
            return trimmed;
        }
        return trimmed;
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (deviceId == null || !supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }

    private void validateSupervisor(AuthenticatedSupervisor supervisor, String supervisorId) {
        if (supervisorId == null || supervisorId.isBlank()) {
            return;
        }
        String code = supervisor.getSupervisor().getCode();
        String id = supervisor.getSupervisor().getId();
        if (!supervisorId.trim().equals(code) && !supervisorId.trim().equals(id)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "supervisorId does not match authenticated session");
        }
    }

    private static String formatInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
    }
}
