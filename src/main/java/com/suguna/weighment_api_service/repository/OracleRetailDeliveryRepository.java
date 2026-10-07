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
import com.suguna.weighment_api_service.integration.oracle.RetailDeliverySqlClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OracleRetailDeliveryRepository implements RetailDeliveryRepository {

    private final RetailDeliverySqlClient retailDeliverySqlClient;

    public OracleRetailDeliveryRepository(RetailDeliverySqlClient retailDeliverySqlClient) {
        this.retailDeliverySqlClient = retailDeliverySqlClient;
    }

    @Override
    public RetailScheduleContext loadRetailSchedule(String supervisorCode, String scheduleId) {
        return retailDeliverySqlClient.loadRetailSchedule(supervisorCode, scheduleId);
    }

    @Override
    public List<RetailDeliveryOrderSummaryDto> listDeliveryOrders(RetailScheduleContext schedule) {
        return retailDeliverySqlClient.listDeliveryOrders(schedule);
    }

    @Override
    public RetailDeliveryOrderDetailDto getDeliveryOrder(RetailScheduleContext schedule, String retailOrderId) {
        return retailDeliverySqlClient.getDeliveryOrder(schedule, retailOrderId);
    }

    @Override
    public List<RetailLocationDto> listRetailerLocations(RetailScheduleContext schedule) {
        return retailDeliverySqlClient.listRetailerLocations(schedule);
    }

    @Override
    public RetailDeliveryActionResponse markArrived(
            RetailScheduleContext schedule, String retailOrderId, MarkRetailerArrivedRequest request) {
        return retailDeliverySqlClient.markArrived(schedule, retailOrderId, request);
    }

    @Override
    public CompleteRetailDeliveryResponse completeDelivery(
            RetailScheduleContext schedule, String retailOrderId, CompleteRetailDeliveryRequest request) {
        return retailDeliverySqlClient.completeDelivery(schedule, retailOrderId, request);
    }

    @Override
    public Optional<String> findExpectedRetailerOtp(RetailScheduleContext schedule, String retailOrderId) {
        return retailDeliverySqlClient.findExpectedRetailerOtp(schedule, retailOrderId);
    }

    @Override
    public CompleteRetailDeliveryResponse triggerInvoice(String retailOrderId) {
        return retailDeliverySqlClient.triggerInvoice(retailOrderId);
    }

    @Override
    public RetailInvoiceStatusResponse getInvoiceStatus(String retailOrderId) {
        return retailDeliverySqlClient.getInvoiceStatus(retailOrderId);
    }
}
