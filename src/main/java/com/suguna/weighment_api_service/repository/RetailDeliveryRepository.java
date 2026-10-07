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
import java.util.Optional;

public interface RetailDeliveryRepository {

    RetailScheduleContext loadRetailSchedule(String supervisorCode, String scheduleId);

    List<RetailDeliveryOrderSummaryDto> listDeliveryOrders(RetailScheduleContext schedule);

    RetailDeliveryOrderDetailDto getDeliveryOrder(RetailScheduleContext schedule, String retailOrderId);

    List<RetailLocationDto> listRetailerLocations(RetailScheduleContext schedule);

    RetailDeliveryActionResponse markArrived(
            RetailScheduleContext schedule, String retailOrderId, MarkRetailerArrivedRequest request);

    CompleteRetailDeliveryResponse completeDelivery(
            RetailScheduleContext schedule, String retailOrderId, CompleteRetailDeliveryRequest request);

    Optional<String> findExpectedRetailerOtp(RetailScheduleContext schedule, String retailOrderId);

    CompleteRetailDeliveryResponse triggerInvoice(String retailOrderId);

    RetailInvoiceStatusResponse getInvoiceStatus(String retailOrderId);
}
