package com.suguna.weighment_api_service.mapper;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.dto.weighment.schedule.FarmDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyContactDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PhotoRequirementDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleBranchDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleCustomerDto;
import com.suguna.weighment_api_service.integration.oracle.ScheduleDcAddressSqlClient;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDcDocumentDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDcRegisteredOfficeDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDeliveryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleFarmSummaryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOrderDto;
import com.suguna.weighment_api_service.constants.WeighmentWorkflowStep;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleOtpDetailDto;
import com.suguna.weighment_api_service.service.ScheduleWorkflowService;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleRulesDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyOtpDetailDto;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSummaryDto;
import com.suguna.weighment_api_service.util.BirdTypeNormalizer;
import com.suguna.weighment_api_service.util.BranchNameSupport;
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
            SupervisorProfile supervisor,
            String saleType,
            List<ScheduleOrderDto> orders,
            String deliveryStatus) {
        List<ScheduleOrderDto> safeOrders = saleType.equals("RETAIL") ? orders : List.of();

        ScheduleFarmSummaryDto farm = toFarmSummary(schedule);

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
        summary.setBranch(toBranch(schedule, supervisor));
        summary.setFarm(farm);
        summary.setCustomer(toCustomer(schedule));
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

        ScheduleBranchDto branch = toBranch(schedule, supervisor);

        ScheduleVehicleDto vehicle = new ScheduleVehicleDto();
        vehicle.setNumber(schedule.getVehicleNumber());
        vehicle.setDriverName(schedule.getDriverName());
        vehicle.setDriverMobile(schedule.getDriverMobile());

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
        detail.setCustomer(toCustomer(schedule));
        detail.setRules(rules);
        detail.setDeliveries(safeDeliveries);
        detail.setDc(toDcDocument(schedule));
        return detail;
    }

    public static ScheduleOtpDetailDto toOtpDetail(
            ScheduleOtpSqlClient.ScheduleOtpRow row,
            LiftingSchedule schedule,
            String saleType,
            ScheduleWorkflowService workflowService) {
        ScheduleOtpDetailDto otp = emptyOtpDetail(schedule, saleType, workflowService);
        PartyOtpDetailDto farmer = otp.getFarmer();
        farmer.setRequired(
                workflowService.isLiftingStepEnabled(saleType, schedule.getScheduleId(), WeighmentWorkflowStep.OTP_VERIFICATION)
                        && row.isFarmerOtpRequired());
        farmer.setMobile(row.farmerMobile());
        farmer.setOtp(row.farmerOtpValue());

        PartyOtpDetailDto trader = otp.getTrader();
        trader.setRequired(
                workflowService.isLiftingStepEnabled(saleType, schedule.getScheduleId(), WeighmentWorkflowStep.OTP_VERIFICATION)
                        && row.isTraderOtpRequired());
        trader.setMobile(null);
        trader.setOtp(row.traderOtpValue());
        return otp;
    }

    public static ScheduleOtpDetailDto emptyOtpDetail(
            LiftingSchedule schedule, String saleType, ScheduleWorkflowService workflowService) {
        PartyOtpDetailDto farmer = new PartyOtpDetailDto();
        farmer.setRequired(
                workflowService.isLiftingStepEnabled(saleType, schedule.getScheduleId(), WeighmentWorkflowStep.OTP_VERIFICATION)
                        && false);

        PartyOtpDetailDto trader = new PartyOtpDetailDto();
        trader.setRequired(false);

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

    /**
     * DC print payload. Only {@code breedStrain} is filled from current lifting row when product/bird type exists;
     * remaining keys stay null until ERP/admin is wired.
     */
    static ScheduleDcDocumentDto toDcDocument(LiftingSchedule schedule) {
        String breed = firstNonBlank(schedule.getProductName(), mapBirdType(schedule));
        return ScheduleDcDocumentDto.builder().breedStrain(blankToNull(breed)).build();
    }

    static ScheduleCustomerDto toCustomer(LiftingSchedule schedule) {
        return ScheduleCustomerDto.builder()
                .code(blankToNull(schedule.getTraderId()))
                .name(blankToNull(schedule.getTraderName()))
                .location(blankToNull(schedule.getCustomerLocation()))
                .build();
    }

    public static void applyBranchAddress(
            ScheduleBranchDto branch, ScheduleDcAddressSqlClient.PartyAddress address) {
        if (branch == null || address == null) {
            return;
        }
        branch.setAddress(address.address());
        branch.setCity(address.city());
        branch.setPostalCode(address.postalCode());
        branch.setState(address.state());
        branch.setGstNo(address.gstNo());
    }

    public static void applyRegisteredOffice(
            ScheduleDcDocumentDto dc, ScheduleDcRegisteredOfficeDto office) {
        if (dc == null || office == null) {
            return;
        }
        dc.setRegisteredOffice(office);
        if (dc.getCinNo() == null && office.getCinNo() != null) {
            dc.setCinNo(office.getCinNo());
        }
        if (dc.getPanNo() == null && office.getPan() != null) {
            dc.setPanNo(office.getPan());
        }
        applyDcRegionCompany(dc, dc.getRegion(), office.getName());
    }

    public static void applyDcRegionCompany(ScheduleDcDocumentDto dc, String region, String company) {
        if (dc == null) {
            return;
        }
        if (region != null && !region.isBlank()) {
            dc.setRegion(region.trim());
        }
        if (company != null && !company.isBlank()) {
            dc.setCompany(company.trim());
        }
        String regionPart = dc.getRegion();
        String companyPart = dc.getCompany();
        if (regionPart != null && companyPart != null) {
            dc.setRegionCompany(regionPart + "-" + companyPart);
        } else if (companyPart != null) {
            dc.setRegionCompany(companyPart);
        } else if (regionPart != null) {
            dc.setRegionCompany(regionPart);
        }
    }

    public static void applyCustomerAddress(
            ScheduleCustomerDto customer, ScheduleDcAddressSqlClient.PartyAddress address) {
        if (customer == null || address == null) {
            return;
        }
        customer.setAddress(address.address());
        customer.setCity(address.city());
        customer.setPostalCode(address.postalCode());
        customer.setState(address.state());
        customer.setGstNo(address.gstNo());
    }

    static ScheduleFarmSummaryDto toFarmSummary(LiftingSchedule schedule) {
        ScheduleFarmSummaryDto farm = new ScheduleFarmSummaryDto();
        farm.setId(schedule.getFarmId());
        farm.setName(schedule.getFarmName());
        farm.setLocation(schedule.getFarmLocation());
        farm.setLatitude(nullIfZero(schedule.getFarmLatitude()));
        farm.setLongitude(nullIfZero(schedule.getFarmLongitude()));
        if (schedule.getGeofenceRadiusMeters() > 0) {
            farm.setGeofenceRadiusMeters(schedule.getGeofenceRadiusMeters());
        }
        return farm;
    }

    static ScheduleBranchDto toBranch(LiftingSchedule schedule, SupervisorProfile supervisor) {
        String fullName = supervisor.getBranchName();
        ScheduleBranchDto branch = new ScheduleBranchDto();
        branch.setId(resolveBranchId(schedule, supervisor));
        branch.setCode(schedule.getBranchCode());
        branch.setName(fullName);
        branch.setShortName(BranchNameSupport.deriveShortName(fullName));
        return branch;
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

    private static Double nullIfZero(double value) {
        return value == 0D ? null : value;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
