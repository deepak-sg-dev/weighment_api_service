package com.suguna.weighment_api_service.mapper;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.dto.weighment.schedule.FarmDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyContactDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PhotoRequirementDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleBranchDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDeliveryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleFarmSummaryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOrderDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleRulesDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSummaryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSupervisorDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ScheduleMapper {

    private static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter SCHEDULED_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private ScheduleMapper() {}

    public static ScheduleSummaryDto toSummary(
            LiftingSchedule schedule,
            String saleType,
            List<ScheduleOrderDto> orders,
            String deliveryStatus) {
        List<ScheduleOrderDto> safeOrders = saleType.equals("RETAIL") ? orders : List.of();
        return ScheduleSummaryDto.builder()
                .scheduleId(schedule.getScheduleId())
                .status(mapDownloadStatus(schedule))
                .downloaded(schedule.isDownloaded())
                .saleType(saleType)
                .birdType(mapBirdType(schedule))
                .scheduledAt(formatScheduledAt(schedule.getScheduledAt()))
                .plannedQuantity(schedule.getPlannedQuantityKg())
                .scaleType(schedule.getScaleType())
                .liftingStatus(mapLiftingStatus(schedule.getStatus()))
                .deliveryStatus(deliveryStatus)
                .farm(ScheduleFarmSummaryDto.builder()
                        .id(schedule.getFarmId())
                        .name(schedule.getFarmName())
                        .location(schedule.getFarmLocation())
                        .build())
                .orderCount(safeOrders.size())
                .orders(safeOrders)
                .build();
    }

    public static ScheduleDetailDto toDetail(
            LiftingSchedule schedule,
            String saleType,
            List<ScheduleDeliveryDto> deliveries,
            String deliveryStatus,
            SupervisorProfile supervisor) {
        List<ScheduleDeliveryDto> safeDeliveries = saleType.equals("RETAIL") ? deliveries : List.of();
        return ScheduleDetailDto.builder()
                .scheduleId(schedule.getScheduleId())
                .status(mapDownloadStatus(schedule))
                .downloaded(schedule.isDownloaded())
                .saleType(saleType)
                .birdType(mapBirdType(schedule))
                .scheduledAt(formatScheduledAt(schedule.getScheduledAt()))
                .plannedQuantity(schedule.getPlannedQuantityKg())
                .scaleType(schedule.getScaleType())
                .liftingStatus(mapLiftingStatus(schedule.getStatus()))
                .deliveryStatus(deliveryStatus)
                .liftStage(mapLiftStage(schedule.getStatus()))
                .branch(ScheduleBranchDto.builder()
                        .id(supervisor.getBranchId())
                        .code(schedule.getBranchCode())
                        .name(supervisor.getBranchName())
                        .build())
                .salesOrderNo(formatSalesOrderNo(schedule.getSalesOrderNo()))
                .declaredBodyWeightKg(
                        schedule.getDeclaredBodyWeightKg() > 0 ? schedule.getDeclaredBodyWeightKg() : null)
                .vehicle(ScheduleVehicleDto.builder()
                        .number(schedule.getVehicleNumber())
                        .build())
                .supervisor(ScheduleSupervisorDto.builder()
                        .code(supervisor.getCode())
                        .name(supervisor.getName())
                        .build())
                .farm(FarmDto.builder()
                        .id(schedule.getFarmId())
                        .name(schedule.getFarmName())
                        .location(schedule.getFarmLocation())
                        .latitude(schedule.getFarmLatitude())
                        .longitude(schedule.getFarmLongitude())
                        .geofenceRadiusMeters(schedule.getGeofenceRadiusMeters())
                        .build())
                .farmer(PartyContactDto.builder()
                        .id(schedule.getFarmerId())
                        .name(schedule.getFarmerName())
                        .mobile(schedule.getFarmerMobile())
                        .build())
                .rules(ScheduleRulesDto.builder()
                        .standardBirdsPerCage(schedule.getStandardBirdsPerCage())
                        .stableDurationMs(schedule.getStableDurationMs())
                        .toleranceMin(schedule.getToleranceMin())
                        .toleranceMax(schedule.getToleranceMax())
                        .captureMode(schedule.getCaptureMode())
                        .photoRequirement(PhotoRequirementDto.builder()
                                .completion(schedule.isCompletionPhotoRequired())
                                .build())
                        .build())
                .deliveries(safeDeliveries)
                .build();
    }

    static String mapDownloadStatus(LiftingSchedule schedule) {
        if (schedule.isDownloaded() || schedule.getStatus() == ScheduleStatus.DOWNLOADED) {
            return "DOWNLOADED";
        }
        return "ASSIGNED";
    }

    static String mapLiftingStatus(ScheduleStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "IN_PROGRESS";
            case COMPLETED, CLOSED -> "COMPLETED";
            default -> "PENDING";
        };
    }

    static String mapLiftStage(ScheduleStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "WEIGHING";
            case COMPLETED, CLOSED -> "COMPLETED";
            default -> "SCHEDULED";
        };
    }

    static String mapBirdType(LiftingSchedule schedule) {
        String raw = schedule.getOperationType();
        if (raw == null || raw.isBlank()) {
            return "BROILER";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    static String formatScheduledAt(Instant instant) {
        Instant value = instant == null ? Instant.now() : instant;
        return value.atZone(INDIA).format(SCHEDULED_AT);
    }

    static String formatSalesOrderNo(Long orderNumber) {
        if (orderNumber == null || orderNumber <= 0) {
            return null;
        }
        return String.valueOf(orderNumber);
    }
}
