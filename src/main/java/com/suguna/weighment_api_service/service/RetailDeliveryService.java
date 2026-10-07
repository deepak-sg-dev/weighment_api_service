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
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
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

    private RetailScheduleContext loadSchedule(AuthenticatedSupervisor supervisor, String scheduleId) {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new BadRequestException("scheduleId is required");
        }
        return retailDeliveryRepository.loadRetailSchedule(supervisor.getSupervisor().getCode(), scheduleId);
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
        if (!erpProperties.isRetailOtpValidationEnabled()) {
            return;
        }
        var expected = retailDeliveryRepository.findExpectedRetailerOtp(schedule, retailOrderId);
        if (expected.isEmpty()) {
            return;
        }
        if (retailerOtp == null || !expected.get().equals(retailerOtp.trim())) {
            throw new BusinessValidationException(ErrorCodes.INVALID_RETAILER_OTP, "Invalid retailer OTP");
        }
    }
}
