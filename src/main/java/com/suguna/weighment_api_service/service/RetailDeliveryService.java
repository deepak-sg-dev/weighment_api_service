package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryRequest;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryResponse;
import com.suguna.weighment_api_service.dto.retail.MarkRetailerArrivedRequest;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryActionResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderDetailResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderListResponse;
import com.suguna.weighment_api_service.dto.retail.RetailInvoiceStatusResponse;
import com.suguna.weighment_api_service.dto.retail.RetailLocationsResponse;
import com.suguna.weighment_api_service.dto.retail.TriggerRetailInvoiceRequest;
import com.suguna.weighment_api_service.dto.retail.UpdateRetailDeliveryVehicleRequest;
import com.suguna.weighment_api_service.dto.retail.UpdateRetailDeliveryVehicleResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class RetailDeliveryService {

    private final RetailDeliveryRepository retailDeliveryRepository;
    private final OracleErpProperties erpProperties;

    public RetailDeliveryService(
            RetailDeliveryRepository retailDeliveryRepository, OracleErpProperties erpProperties) {
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.erpProperties = erpProperties;
    }

    public RetailDeliveryOrderListResponse listDeliveryOrders(
            AuthenticatedSupervisor supervisor, String scheduleId) {
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);
        return RetailDeliveryOrderListResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .orders(retailDeliveryRepository.listDeliveryOrders(schedule))
                .build();
    }

    public RetailDeliveryOrderDetailResponse getDeliveryOrder(
            AuthenticatedSupervisor supervisor, String retailOrderId, String scheduleId) {
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);
        return RetailDeliveryOrderDetailResponse.builder()
                .success(true)
                .order(retailDeliveryRepository.getDeliveryOrder(schedule, retailOrderId))
                .build();
    }

    public RetailLocationsResponse listRetailerLocations(AuthenticatedSupervisor supervisor, String scheduleId) {
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);
        return RetailLocationsResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .locations(retailDeliveryRepository.listRetailerLocations(schedule))
                .build();
    }

    public RetailDeliveryActionResponse markArrived(
            AuthenticatedSupervisor supervisor,
            String retailOrderId,
            String scheduleId,
            MarkRetailerArrivedRequest request) {
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);
        return retailDeliveryRepository.markArrived(schedule, retailOrderId, request);
    }

    public CompleteRetailDeliveryResponse completeDelivery(
            AuthenticatedSupervisor supervisor, String retailOrderId, CompleteRetailDeliveryRequest request) {
        RetailScheduleContext schedule = loadSchedule(supervisor, request.getScheduleId());
        validateCompletionRequest(request);
        validateRetailerOtp(schedule, retailOrderId, request.getRetailerOtp());
        return retailDeliveryRepository.completeDelivery(schedule, retailOrderId, request);
    }

    public CompleteRetailDeliveryResponse triggerInvoice(
            AuthenticatedSupervisor supervisor,
            String retailOrderId,
            TriggerRetailInvoiceRequest request) {
        return retailDeliveryRepository.triggerInvoice(retailOrderId);
    }

    public RetailInvoiceStatusResponse getInvoiceStatus(AuthenticatedSupervisor supervisor, String retailOrderId) {
        return retailDeliveryRepository.getInvoiceStatus(retailOrderId);
    }

    public UpdateRetailDeliveryVehicleResponse updateDeliveryVehicle(
            AuthenticatedSupervisor supervisor,
            String retailOrderId,
            String scheduleId,
            UpdateRetailDeliveryVehicleRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        RetailScheduleContext schedule = loadSchedule(supervisor, scheduleId);
        var order = retailDeliveryRepository.getDeliveryOrder(schedule, retailOrderId);
        ensureDeliveryLineOpen(order.getDeliveryStatus());
        UpdateRetailDeliveryVehicleRequest.VehiclePayload vehicle = request.getVehicle();
        retailDeliveryRepository.updateDeliveryVehicle(
                schedule,
                retailOrderId,
                vehicle.getNumber().trim().toUpperCase(),
                vehicle.getDriverName().trim(),
                vehicle.getDriverMobile().trim());
        ScheduleVehicleDto dto = new ScheduleVehicleDto();
        dto.setNumber(vehicle.getNumber().trim().toUpperCase());
        dto.setDriverName(vehicle.getDriverName().trim());
        dto.setDriverMobile(vehicle.getDriverMobile().trim());
        return UpdateRetailDeliveryVehicleResponse.builder()
                .success(true)
                .message("Delivery vehicle updated")
                .scheduleId(ScheduleIdSupport.normalize(scheduleId))
                .retailOrderId(retailOrderId)
                .deliveryVehicle(dto)
                .updatedAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                        Instant.now().atOffset(ZoneOffset.ofHoursMinutes(5, 30))))
                .build();
    }

    private static void ensureDeliveryLineOpen(String deliveryStatus) {
        if (deliveryStatus != null && "CANCELLED".equalsIgnoreCase(deliveryStatus.trim())) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Delivery order is cancelled");
        }
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (!supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }

    private RetailScheduleContext loadSchedule(AuthenticatedSupervisor supervisor, String scheduleId) {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new BadRequestException("scheduleId is required");
        }
        return retailDeliveryRepository.loadRetailSchedule(
                supervisor.getSupervisor().getCode(), ScheduleIdSupport.normalize(scheduleId));
    }

    private void validateCompletionRequest(CompleteRetailDeliveryRequest request) {
        if (request.getDeliveryLocation() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.DELIVERY_LOCATION_REQUIRED, "Delivery location is required");
        }
        if (request.getShopImage() == null
                || request.getShopImage().getReference() == null
                || request.getShopImage().getReference().isBlank()) {
            throw new BusinessValidationException(ErrorCodes.SHOP_IMAGE_REQUIRED, "Shop image reference is required");
        }
    }

    private void validateRetailerOtp(RetailScheduleContext schedule, String retailOrderId, String retailerOtp) {
        RetailRetailerOtpValidation.validate(
                erpProperties, retailDeliveryRepository.resolveRetailerOtp(schedule, retailOrderId), retailerOtp);
    }
}
