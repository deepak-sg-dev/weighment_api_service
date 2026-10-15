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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<ScheduleDeliveryDto> deliveries = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            RetailRow row = rows.get(i);
            deliveries.add(ScheduleDeliveryDto.builder()
                    .deliveryId(row.order().getRetailOrderId())
                    .sequence(i + 1)
                    .dropPointName(
                            row.order().getRetailerName() == null
                                    ? row.order().getRetailerId()
                                    : row.order().getRetailerName())
                    .customerCode(row.order().getRetailerId())
                    .customerName(row.order().getRetailerName())
                    .location(row.location().getAddress())
                    .latitude(nullIfZero(row.location().getLatitude()))
                    .longitude(nullIfZero(row.location().getLongitude()))
                    .geofenceRadiusMeters(schedule.getGeofenceRadiusMeters())
                    .quantityKg(row.order().getOrderedQuantity())
                    .status(mapDeliveryStatus(row.order().getStatus()))
                    .build());
        }
        return deliveries;
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
        return RetailLocationDto.builder()
                .retailOrderId(order.getRetailOrderId())
                .retailerId(order.getRetailerId())
                .retailerName(order.getRetailerName())
                .latitude(0)
                .longitude(0)
                .address(null)
                .build();
    }

    private ScheduleOrderDto toListOrder(RetailRow row) {
        return ScheduleOrderDto.builder()
                .customerCode(row.order().getRetailerId())
                .customerName(row.order().getRetailerName())
                .orderedQuantityKg(row.order().getOrderedQuantity())
                .location(row.location().getAddress())
                .latitude(nullIfZero(row.location().getLatitude()))
                .longitude(nullIfZero(row.location().getLongitude()))
                .build();
    }

    private static Double nullIfZero(double value) {
        return value == 0D ? null : value;
    }

    private String aggregateRetailRowStatuses(List<RetailRow> rows) {
        List<String> statuses = rows.stream()
                .map(row -> mapDeliveryStatus(row.order().getStatus()))
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
