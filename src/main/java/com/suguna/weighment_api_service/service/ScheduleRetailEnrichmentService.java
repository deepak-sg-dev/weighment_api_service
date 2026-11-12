package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.WeighmentWorkflowStep;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyOtpDetailDto;
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpResolution;
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpSupport;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderSummaryDto;
import com.suguna.weighment_api_service.dto.retail.RetailLocationDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDeliveryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOrderDto;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class ScheduleRetailEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleRetailEnrichmentService.class);

    private final RetailDeliveryRepository retailDeliveryRepository;
    private final OracleErpProperties erpProperties;
    private final ScheduleWorkflowService scheduleWorkflowService;

    public ScheduleRetailEnrichmentService(
            RetailDeliveryRepository retailDeliveryRepository,
            OracleErpProperties erpProperties,
            ScheduleWorkflowService scheduleWorkflowService) {
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.erpProperties = erpProperties;
        this.scheduleWorkflowService = scheduleWorkflowService;
    }

    public boolean isRetail(LiftingSchedule schedule) {
        return resolveSaleType(schedule.getOrderSource()).equals("RETAIL");
    }

    public String resolveSaleType(String orderSource) {
        if (orderSource != null && orderSource.trim().equalsIgnoreCase(erpProperties.getRetailLiftingSource())) {
            return "RETAIL";
        }
        return "WHOLESALE";
    }

    public List<ScheduleOrderDto> loadListOrders(String supervisorCode, LiftingSchedule schedule) {
        return loadRetailRows(supervisorCode, schedule).stream()
                .map(row -> toListOrder(row.order()))
                .toList();
    }

    /** Batch load for {@code GET /weighment/schedules} — one Oracle query for all retail schedule ids. */
    public Map<String, List<ScheduleOrderDto>> loadListOrdersBatch(String supervisorCode, List<String> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return Map.of();
        }
        Map<String, List<RetailDeliveryOrderSummaryDto>> grouped =
                retailDeliveryRepository.listDeliveryOrdersGroupedBySchedule(supervisorCode, scheduleIds);
        Map<String, List<ScheduleOrderDto>> result = new HashMap<>();
        grouped.forEach((scheduleKey, orders) ->
                result.put(scheduleKey, orders.stream().map(this::toListOrder).toList()));
        return result;
    }

    public String aggregateDeliveryStatusFromOrders(List<ScheduleOrderDto> orders, String saleType) {
        if (!"RETAIL".equals(saleType)) {
            return "NOT_APPLICABLE";
        }
        if (orders == null || orders.isEmpty()) {
            return "PENDING";
        }
        List<String> statuses = orders.stream()
                .map(order -> order.getDeliveryStatus() != null ? order.getDeliveryStatus() : "PENDING")
                .toList();
        if (statuses.stream().allMatch("COMPLETED"::equals)) {
            return "COMPLETED";
        }
        if (statuses.stream().anyMatch(s -> "IN_PROGRESS".equals(s) || "COMPLETED".equals(s))) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    public List<ScheduleDeliveryDto> loadDetailDeliveries(String supervisorCode, LiftingSchedule schedule) {
        List<RetailRow> rows = loadRetailRows(supervisorCode, schedule);
        return IntStream.range(0, rows.size())
                .mapToObj(i -> toDeliveryDto(schedule, rows.get(i), i + 1))
                .toList();
    }

    public String resolveDeliveryStatus(String supervisorCode, LiftingSchedule schedule) {
        if (!isRetail(schedule)) {
            return "NOT_APPLICABLE";
        }
        List<RetailRow> rows = loadRetailRows(supervisorCode, schedule);
        if (rows.isEmpty()) {
            return "PENDING";
        }
        return aggregateRetailRowStatuses(rows);
    }

    public String resolveDeliveryStatus(List<ScheduleDeliveryDto> deliveries, String saleType) {
        if (!"RETAIL".equals(saleType)) {
            return "NOT_APPLICABLE";
        }
        if (deliveries.isEmpty()) {
            return "PENDING";
        }
        if (deliveries.stream().allMatch(d -> "COMPLETED".equals(d.getStatus()))) {
            return "COMPLETED";
        }
        if (deliveries.stream().anyMatch(d -> "IN_PROGRESS".equals(d.getStatus()) || "COMPLETED".equals(d.getStatus()))) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    private List<RetailRow> loadRetailRows(String supervisorCode, LiftingSchedule schedule) {
        if (!isRetail(schedule)) {
            return List.of();
        }
        RetailScheduleContext context = retailContextFromListRow(schedule, supervisorCode);
        List<RetailDeliveryOrderSummaryDto> orders = retailDeliveryRepository.listDeliveryOrders(context);
        if (orders.isEmpty()) {
            log.warn(
                    "No retail delivery lines for schedule {} (supervisor {}, source {}). "
                            + "Check OE_PO_CHAIN link and HZ ship-to for this indent in ERP.",
                    schedule.getScheduleId(),
                    supervisorCode,
                    schedule.getOrderSource());
            return List.of();
        }
        return orders.stream().map(order -> new RetailRow(order, locationFromSummary(order))).toList();
    }

    /** Reuses the lifting row already returned by the schedule list (same supervisor + schedule id scope). */
    private RetailScheduleContext retailContextFromListRow(LiftingSchedule schedule, String supervisorCode) {
        String liftingSupervisor =
                schedule.getSupervisorId() != null && !schedule.getSupervisorId().isBlank()
                        ? schedule.getSupervisorId()
                        : supervisorCode;
        return RetailScheduleContext.builder()
                .scheduleId(schedule.getScheduleId())
                .supervisorCode(liftingSupervisor)
                .regionCode(null)
                .retailBranchCode("")
                .orderDate(schedule.getScheduledAt())
                .internalPoNumber(schedule.getSalesOrderNo())
                .build();
    }

    private static RetailLocationDto locationFromSummary(RetailDeliveryOrderSummaryDto order) {
        RetailLocationDto location = new RetailLocationDto();
        location.setRetailOrderId(order.getRetailOrderId());
        location.setRetailerId(order.getRetailerId());
        location.setRetailerName(order.getRetailerName());
        location.setAddress(order.getRetailerAddress());
        location.setLatitude(order.getRetailerLatitude() != null ? order.getRetailerLatitude() : 0D);
        location.setLongitude(order.getRetailerLongitude() != null ? order.getRetailerLongitude() : 0D);
        return location;
    }

    private ScheduleOrderDto toListOrder(RetailDeliveryOrderSummaryDto order) {
        ScheduleOrderDto dto = new ScheduleOrderDto();
        dto.setOrderNo(order.getRetailOrderId());
        dto.setCustomerCode(order.getRetailerId());
        dto.setCustomerName(order.getRetailerName());
        dto.setOrderedQuantityKg(order.getOrderedQuantity());
        dto.setOrderedBirdCount(order.getOrderedBirdCount());
        dto.setDeliveredQuantityKg(order.getDeliveredQuantityKg());
        dto.setDeliveredBirdCount(order.getDeliveredBirdCount());
        dto.setLocation(order.getRetailerAddress());
        dto.setLatitude(order.getRetailerLatitude());
        dto.setLongitude(order.getRetailerLongitude());
        dto.setDeliveryStatus(order.getDeliveryStatus());
        dto.setDeliveryStartedAt(order.getDeliveryStartedAt());
        dto.setDeliveryVehicle(order.getDeliveryVehicle());
        return dto;
    }

    private ScheduleDeliveryDto toDeliveryDto(LiftingSchedule schedule, RetailRow row, int sequence) {
        RetailDeliveryOrderSummaryDto order = row.order();
        String dropPointName = order.getRetailerName() == null ? order.getRetailerId() : order.getRetailerName();
        ScheduleDeliveryDto dto = new ScheduleDeliveryDto();
        dto.setDeliveryId(order.getRetailOrderId());
        dto.setSequence(sequence);
        dto.setDropPointName(dropPointName);
        dto.setCustomerCode(order.getRetailerId());
        dto.setCustomerName(order.getRetailerName());
        dto.setLocation(row.location().getAddress());
        dto.setLatitude(nullIfZero(row.location().getLatitude()));
        dto.setLongitude(nullIfZero(row.location().getLongitude()));
        dto.setGeofenceRadiusMeters(schedule.getGeofenceRadiusMeters());
        dto.setQuantityKg(order.getOrderedQuantity());
        dto.setOrderedBirdCount(order.getOrderedBirdCount());
        dto.setDeliveredQuantityKg(order.getDeliveredQuantityKg());
        dto.setDeliveredBirdCount(order.getDeliveredBirdCount());
        String deliveryStatus = order.getDeliveryStatus() != null
                ? order.getDeliveryStatus()
                : mapDeliveryStatus(order.getStatus());
        dto.setStatus(deliveryStatus);
        dto.setDeliveryStatus(deliveryStatus);
        dto.setDeliveryStartedAt(order.getDeliveryStartedAt());
        dto.setCustomerOtp(buildCustomerOtp(schedule, order));
        return dto;
    }

    private PartyOtpDetailDto buildCustomerOtp(
            LiftingSchedule schedule, RetailDeliveryOrderSummaryDto order) {
        boolean required = scheduleWorkflowService.isDeliveryStepEnabled(
                schedule.getScheduleId(), WeighmentWorkflowStep.OTP_VALIDATION);
        PartyOtpDetailDto otp = new PartyOtpDetailDto();
        otp.setRequired(required && erpProperties.isRetailOtpValidationEnabled());
        if (!otp.isRequired()) {
            return otp;
        }
        RetailRetailerOtpResolution resolution = RetailRetailerOtpSupport.resolve(
                order.getRetailOtp(),
                order.getRetailOtpExpiresAt(),
                order.getMessage(),
                Instant.now(),
                erpProperties.getRetailOtpStubCode());
        otp.setOtp(resolution.configured() && !resolution.expired() ? resolution.expectedCode() : null);
        return otp;
    }

    private static Double nullIfZero(double value) {
        return value == 0D ? null : value;
    }

    private String aggregateRetailRowStatuses(List<RetailRow> rows) {
        List<String> statuses = rows.stream()
                .map(row -> {
                    RetailDeliveryOrderSummaryDto order = row.order();
                    return order.getDeliveryStatus() != null
                            ? order.getDeliveryStatus()
                            : mapDeliveryStatus(order.getStatus());
                })
                .toList();
        if (statuses.stream().allMatch("COMPLETED"::equals)) {
            return "COMPLETED";
        }
        if (statuses.stream().anyMatch(s -> "IN_PROGRESS".equals(s) || "COMPLETED".equals(s))) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    static String mapDeliveryStatus(String retailStatus) {
        if (retailStatus == null) {
            return "PENDING";
        }
        String normalized = retailStatus.trim().toUpperCase(Locale.ROOT);
        if ("COMPLETED".equals(normalized)) {
            return "COMPLETED";
        }
        if (normalized.contains("ARRIVED") || normalized.contains("PROGRESS")) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    private record RetailRow(RetailDeliveryOrderSummaryDto order, RetailLocationDto location) {}
}
