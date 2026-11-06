package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryRequest;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryResponse;
import com.suguna.weighment_api_service.dto.retail.MarkRetailerArrivedRequest;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryActionResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderDetailDto;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderSummaryDto;
import com.suguna.weighment_api_service.dto.retail.RetailInvoiceStatusResponse;
import com.suguna.weighment_api_service.dto.retail.RetailLocationDto;

import java.util.List;
import java.util.Map;
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpResolution;

public interface RetailDeliveryRepository {

    RetailScheduleContext loadRetailSchedule(String supervisorCode, String scheduleId);

    List<RetailDeliveryOrderSummaryDto> listDeliveryOrders(RetailScheduleContext schedule);

    Map<String, List<RetailDeliveryOrderSummaryDto>> listDeliveryOrdersGroupedBySchedule(
            String supervisorCode, List<String> scheduleIds);

    RetailDeliveryOrderDetailDto getDeliveryOrder(RetailScheduleContext schedule, String retailOrderId);

    List<RetailLocationDto> listRetailerLocations(RetailScheduleContext schedule);

    RetailDeliveryActionResponse markArrived(
            RetailScheduleContext schedule, String retailOrderId, MarkRetailerArrivedRequest request);

    CompleteRetailDeliveryResponse completeDelivery(
            RetailScheduleContext schedule, String retailOrderId, CompleteRetailDeliveryRequest request);

    RetailRetailerOtpResolution resolveRetailerOtp(RetailScheduleContext schedule, String retailOrderId);

    boolean isRetailerOtpVerified(RetailScheduleContext schedule, String retailOrderId);

    void replaceRetailerOtp(RetailScheduleContext schedule, String retailOrderId, String newOtp);

    void markRetailerOtpVerified(RetailScheduleContext schedule, String retailOrderId);

    CompleteRetailDeliveryResponse triggerInvoice(String retailOrderId);

    RetailInvoiceStatusResponse getInvoiceStatus(String retailOrderId);

    void updateDeliveryVehicle(
            RetailScheduleContext schedule,
            String retailOrderId,
            String vehicleNumber,
            String driverName,
            String driverMobile);
}
