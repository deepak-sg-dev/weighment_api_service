package com.suguna.weighment_api_service.mapper;

import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.FarmDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyContactDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.PhotoRequirementDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ProductDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleRulesDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSummaryDto;

public final class ScheduleMapper {

    private ScheduleMapper() {
    }

    public static ScheduleSummaryDto toSummary(LiftingSchedule schedule) {
        return ScheduleSummaryDto.builder()
                .scheduleId(schedule.getScheduleId())
                .farmId(schedule.getFarmId())
                .farmName(schedule.getFarmName())
                .operationType(schedule.getOperationType())
                .scaleType(schedule.getScaleType())
                .plannedQuantity(schedule.getPlannedQuantity())
                .status(schedule.getStatus().name())
                .downloaded(schedule.isDownloaded())
                .build();
    }

    public static ScheduleDetailDto toDetail(LiftingSchedule schedule) {
        return ScheduleDetailDto.builder()
                .scheduleId(schedule.getScheduleId())
                .farm(FarmDto.builder()
                        .id(schedule.getFarmId())
                        .name(schedule.getFarmName())
                        .latitude(schedule.getFarmLatitude())
                        .longitude(schedule.getFarmLongitude())
                        .geofenceRadiusMeters(schedule.getGeofenceRadiusMeters())
                        .build())
                .farmer(PartyContactDto.builder()
                        .id(schedule.getFarmerId())
                        .name(schedule.getFarmerName())
                        .mobile(schedule.getFarmerMobile())
                        .build())
                .trader(PartyContactDto.builder()
                        .id(schedule.getTraderId())
                        .name(schedule.getTraderName())
                        .mobile(schedule.getTraderMobile())
                        .build())
                .product(ProductDto.builder()
                        .id(schedule.getProductId())
                        .name(schedule.getProductName())
                        .build())
                .plannedQuantity(schedule.getPlannedQuantity())
                .operationType(schedule.getOperationType())
                .scaleType(schedule.getScaleType())
                .status(schedule.getStatus().name())
                .downloaded(schedule.isDownloaded())
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
                .build();
    }
}
