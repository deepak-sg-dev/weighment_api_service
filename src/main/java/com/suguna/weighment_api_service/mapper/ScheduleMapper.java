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
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOtpDetailDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleRulesDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyOtpDetailDto;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSummaryDto;
import com.suguna.weighment_api_service.util.BirdTypeNormalizer;
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

        ScheduleFarmSummaryDto farm = new ScheduleFarmSummaryDto();
        farm.setId(schedule.getFarmId());
        farm.setName(schedule.getFarmName());
        farm.setLocation(schedule.getFarmLocation());

        ScheduleSummaryDto summary = new ScheduleSummaryDto();
        summary.setScheduleId(schedule.getScheduleId());
        summary.setStatus(mapDownloadStatus(schedule));
        summary.setDownloaded(schedule.isDownloaded());
        summary.setSaleType(saleType);
        summary.setBirdType(mapBirdType(schedule));
        summary.setScheduledAt(formatScheduledAt(schedule.getScheduledAt()));
        summary.setStartedAt(formatOptionalInstant(schedule.getStartedAt()));
        summary.setPlannedQuantity(schedule.getPlannedQuantityKg());
        summary.setScaleType(schedule.getScaleType());
        summary.setLiftingStatus(resolveLiftingStatus(schedule));
        summary.setDeliveryStatus(deliveryStatus);
        summary.setFarm(farm);
        summary.setOrderCount(safeOrders.size());
        summary.setOrders(safeOrders);
        return summary;
    }

    public static ScheduleDetailDto toDetail(
            LiftingSchedule schedule,
            String saleType,
            List<ScheduleDeliveryDto> deliveries,
            String deliveryStatus,
            SupervisorProfile supervisor) {
        List<ScheduleDeliveryDto> safeDeliveries = saleType.equals("RETAIL") ? deliveries : List.of();

        ScheduleBranchDto branch = new ScheduleBranchDto();
        branch.setId(resolveBranchId(schedule, supervisor));
        branch.setCode(schedule.getBranchCode());
        branch.setName(supervisor.getBranchName());

        ScheduleVehicleDto vehicle = new ScheduleVehicleDto();
        vehicle.setNumber(schedule.getVehicleNumber());

        ScheduleSupervisorDto scheduleSupervisor = new ScheduleSupervisorDto();
        scheduleSupervisor.setCode(supervisor.getCode());
        scheduleSupervisor.setName(supervisor.getName());

        FarmDto farm = new FarmDto();
        farm.setId(schedule.getFarmId());
        farm.setName(schedule.getFarmName());
        farm.setLocation(schedule.getFarmLocation());
        farm.setLatitude(schedule.getFarmLatitude());
        farm.setLongitude(schedule.getFarmLongitude());
        farm.setGeofenceRadiusMeters(schedule.getGeofenceRadiusMeters());

        PartyContactDto farmer = new PartyContactDto();
        farmer.setId(schedule.getFarmerId());
        farmer.setName(schedule.getFarmerName());
        farmer.setMobile(schedule.getFarmerMobile());

        PhotoRequirementDto photoRequirement = new PhotoRequirementDto();
        photoRequirement.setCompletion(schedule.isCompletionPhotoRequired());

        ScheduleRulesDto rules = new ScheduleRulesDto();
        rules.setStandardBirdsPerCage(schedule.getStandardBirdsPerCage());
        rules.setStableDurationMs(schedule.getStableDurationMs());
        rules.setToleranceMin(schedule.getToleranceMin());
        rules.setToleranceMax(schedule.getToleranceMax());
        rules.setCaptureMode(schedule.getCaptureMode());
        rules.setPhotoRequirement(photoRequirement);

        ScheduleDetailDto detail = new ScheduleDetailDto();
        detail.setScheduleId(schedule.getScheduleId());
        detail.setStatus(mapDownloadStatus(schedule));
        detail.setDownloaded(schedule.isDownloaded());
        detail.setSaleType(saleType);
        detail.setBirdType(mapBirdType(schedule));
        detail.setScheduledAt(formatScheduledAt(schedule.getScheduledAt()));
        detail.setStartedAt(formatOptionalInstant(schedule.getStartedAt()));
        detail.setPlannedQuantity(schedule.getPlannedQuantityKg());
        detail.setScaleType(schedule.getScaleType());
        detail.setLiftingStatus(resolveLiftingStatus(schedule));
        detail.setDeliveryStatus(deliveryStatus);
        detail.setBranch(branch);
        detail.setSalesOrderNo(formatSalesOrderNo(schedule.getSalesOrderNo()));
        detail.setDeclaredBodyWeightKg(
                schedule.getDeclaredBodyWeightKg() > 0 ? schedule.getDeclaredBodyWeightKg() : null);
        detail.setVehicle(vehicle);
        detail.setSupervisor(scheduleSupervisor);
        detail.setFarm(farm);
        detail.setFarmer(farmer);
        detail.setRules(rules);
        detail.setDeliveries(safeDeliveries);
        return detail;
    }

    public static ScheduleOtpDetailDto toOtpDetail(ScheduleOtpSqlClient.ScheduleOtpRow row) {
        PartyOtpDetailDto farmer = new PartyOtpDetailDto();
        farmer.setRequired(row.isFarmerOtpRequired());
        farmer.setMobile(row.farmerMobile());
        farmer.setOtp(row.farmerOtpValue());

        PartyOtpDetailDto trader = new PartyOtpDetailDto();
        trader.setRequired(row.isTraderOtpRequired());
        trader.setMobile(null);
        trader.setOtp(row.traderOtpValue());

        ScheduleOtpDetailDto otp = new ScheduleOtpDetailDto();
        otp.setFarmer(farmer);
        otp.setTrader(trader);
        return otp;
    }

    /** List/detail tab label aligned with {@code lifting_status} / download flag. */
    static String mapDownloadStatus(LiftingSchedule schedule) {
        String lifting = resolveLiftingStatus(schedule);
        if ("COMPLETED".equals(lifting) || "CANCELLED".equals(lifting)) {
            return "COMPLETED".equals(lifting) ? "COMPLETED" : lifting;
        }
        if ("IN_PROGRESS".equals(lifting)) {
            return "IN_PROGRESS";
        }
        if (schedule.isDownloaded() || schedule.getStatus() == ScheduleStatus.DOWNLOADED || "DOWNLOADED".equals(lifting)) {
            return "DOWNLOADED";
        }
        return "PENDING";
    }

    static String resolveLiftingStatus(LiftingSchedule schedule) {
        if (schedule.getLiftingStatus() != null && !schedule.getLiftingStatus().isBlank()) {
            return schedule.getLiftingStatus().trim().toUpperCase(Locale.ROOT);
        }
        return mapLiftingStatusFromEnum(schedule.getStatus());
    }

    static String mapLiftingStatusFromEnum(ScheduleStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "IN_PROGRESS";
            case DOWNLOADED -> "DOWNLOADED";
            case COMPLETED, CLOSED -> "COMPLETED";
            default -> "PENDING";
        };
    }

    static String formatOptionalInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(INDIA).format(SCHEDULED_AT);
    }

    static String resolveBranchId(LiftingSchedule schedule, SupervisorProfile supervisor) {
        if (schedule.getBranchId() != null && !schedule.getBranchId().isBlank()) {
            return schedule.getBranchId().trim();
        }
        if (supervisor.getBranchId() != null && !supervisor.getBranchId().isBlank()) {
            return supervisor.getBranchId().trim();
        }
        return schedule.getBranchCode();
    }

    static String mapBirdType(LiftingSchedule schedule) {
        return BirdTypeNormalizer.fromOrderType(schedule.getOperationType());
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
