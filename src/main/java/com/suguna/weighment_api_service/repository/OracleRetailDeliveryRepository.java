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
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpResolution;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
    public Map<String, List<RetailDeliveryOrderSummaryDto>> listDeliveryOrdersGroupedBySchedule(
            String supervisorCode, List<String> scheduleIds) {
        return retailDeliverySqlClient.listDeliveryOrdersGroupedBySchedule(supervisorCode, scheduleIds);
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
    public RetailRetailerOtpResolution resolveRetailerOtp(RetailScheduleContext schedule, String retailOrderId) {
        return retailDeliverySqlClient.resolveRetailerOtp(schedule, retailOrderId);
    }

    @Override
    public boolean isRetailerOtpVerified(RetailScheduleContext schedule, String retailOrderId) {
        return retailDeliverySqlClient.isRetailerOtpVerified(schedule, retailOrderId);
    }

    @Override
    public void replaceRetailerOtp(RetailScheduleContext schedule, String retailOrderId, String newOtp) {
        retailDeliverySqlClient.replaceRetailerOtp(schedule, retailOrderId, newOtp);
    }

    @Override
    public void markRetailerOtpVerified(RetailScheduleContext schedule, String retailOrderId) {
        retailDeliverySqlClient.markRetailerOtpVerified(schedule, retailOrderId);
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
