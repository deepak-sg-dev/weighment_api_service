package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderSummaryDto;
import com.suguna.weighment_api_service.dto.retail.RetailLocationDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDeliveryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOrderDto;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ScheduleRetailEnrichmentService {

    private final RetailDeliveryRepository retailDeliveryRepository;
    private final OracleErpProperties erpProperties;

    public ScheduleRetailEnrichmentService(
            RetailDeliveryRepository retailDeliveryRepository, OracleErpProperties erpProperties) {
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.erpProperties = erpProperties;
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
        return loadRetailRows(supervisorCode, schedule).stream().map(this::toListOrder).toList();
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
        try {
            RetailScheduleContext context =
                    retailDeliveryRepository.loadRetailSchedule(supervisorCode, schedule.getScheduleId());
            List<RetailDeliveryOrderSummaryDto> orders = retailDeliveryRepository.listDeliveryOrders(context);
            List<RetailLocationDto> locations = retailDeliveryRepository.listRetailerLocations(context);
            Map<String, RetailLocationDto> byOrder = locations.stream()
                    .collect(Collectors.toMap(
                            RetailLocationDto::getRetailOrderId, loc -> loc, (a, b) -> a));
            return orders.stream()
                    .map(order -> new RetailRow(order, byOrder.getOrDefault(order.getRetailOrderId(), emptyLocation(order))))
                    .toList();
        } catch (ResourceNotFoundException ex) {
            return List.of();
        }
    }

    private static RetailLocationDto emptyLocation(RetailDeliveryOrderSummaryDto order) {
        RetailLocationDto location = new RetailLocationDto();
        location.setRetailOrderId(order.getRetailOrderId());
        location.setRetailerId(order.getRetailerId());
        location.setRetailerName(order.getRetailerName());
        location.setLatitude(0);
        location.setLongitude(0);
        location.setAddress(null);
        return location;
    }

    private ScheduleOrderDto toListOrder(RetailRow row) {
        ScheduleOrderDto dto = new ScheduleOrderDto();
        dto.setOrderNo(row.order().getRetailOrderId());
        dto.setCustomerCode(row.order().getRetailerId());
        dto.setCustomerName(row.order().getRetailerName());
        dto.setOrderedQuantityKg(row.order().getOrderedQuantity());
        dto.setLocation(row.location().getAddress());
        dto.setLatitude(nullIfZero(row.location().getLatitude()));
        dto.setLongitude(nullIfZero(row.location().getLongitude()));
        dto.setDeliveryStatus(row.order().getDeliveryStatus());
        dto.setDeliveryStartedAt(row.order().getDeliveryStartedAt());
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
        String deliveryStatus = order.getDeliveryStatus() != null
                ? order.getDeliveryStatus()
                : mapDeliveryStatus(order.getStatus());
        dto.setStatus(deliveryStatus);
        dto.setDeliveryStatus(deliveryStatus);
        dto.setDeliveryStartedAt(order.getDeliveryStartedAt());
        return dto;
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
