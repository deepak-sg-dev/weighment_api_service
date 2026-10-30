package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleActionResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListFilter;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListStatusFilter;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSummaryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailDto;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient;
import com.suguna.weighment_api_service.mapper.ScheduleMapper;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleRetailEnrichmentService retailEnrichmentService;
    private final ScheduleOtpSqlClient scheduleOtpSqlClient;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            ScheduleRetailEnrichmentService retailEnrichmentService,
            ScheduleOtpSqlClient scheduleOtpSqlClient) {
        this.scheduleRepository = scheduleRepository;
        this.retailEnrichmentService = retailEnrichmentService;
        this.scheduleOtpSqlClient = scheduleOtpSqlClient;
    }

    public ScheduleListResponse getAssignedSchedules(
            AuthenticatedSupervisor supervisor, String statusCsv, Integer lookbackDays, String scheduleIdsCsv) {
        ScheduleListStatusFilter statusFilter = ScheduleListStatusFilter.fromQuery(statusCsv);
        if (!statusFilter.isValid()) {
            throw new BadRequestException(
                    "Invalid status filter. Allowed: all, pending, downloaded, in_progress, completed "
                            + "(comma-separated for multiple tabs)");
        }
        if (lookbackDays != null && (lookbackDays < 0 || lookbackDays > 366)) {
            throw new BadRequestException("lookbackDays must be between 0 and 366");
        }
        ScheduleListFilter listFilter;
        try {
            listFilter = ScheduleListFilter.fromQuery(lookbackDays, scheduleIdsCsv);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        String supervisorCode = supervisor.getSupervisor().getCode();
        List<LiftingSchedule> schedules = scheduleRepository.findBySupervisorIdAndStatusIn(
                supervisorCode, statusFilter.statuses(), listFilter, statusFilter.isIncludeAll());

        List<ScheduleSummaryDto> summaries = schedules.stream()
                .map(schedule -> {
                    String saleType = retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
                    var orders = retailEnrichmentService.loadListOrders(supervisorCode, schedule);
                    String deliveryStatus = resolveScheduleDeliveryStatus(
                            schedule,
                            saleType,
                            retailEnrichmentService.resolveDeliveryStatus(supervisorCode, schedule));
                    return ScheduleMapper.toSummary(schedule, saleType, orders, deliveryStatus);
                })
                .toList();
        return ScheduleListResponse.success(summaries);
    }

    public ScheduleDetailResponse getScheduleDetails(AuthenticatedSupervisor supervisor, String scheduleId) {
        LiftingSchedule schedule = loadSchedule(supervisor, scheduleId);
        String supervisorCode = supervisor.getSupervisor().getCode();
        String saleType = retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
        var deliveries = retailEnrichmentService.loadDetailDeliveries(supervisorCode, schedule);
        String deliveryStatus = resolveScheduleDeliveryStatus(
                schedule, saleType, retailEnrichmentService.resolveDeliveryStatus(deliveries, saleType));
        ScheduleDetailDto detail = ScheduleMapper.toDetail(
                schedule, saleType, deliveries, deliveryStatus, supervisor.getSupervisor());
        scheduleOtpSqlClient
                .findOtpContext(supervisorCode, schedule.getScheduleId())
                .ifPresent(row -> detail.setOtp(ScheduleMapper.toOtpDetail(row)));
        return ScheduleDetailResponse.success(detail);
    }

    public ScheduleActionResponse markDownloaded(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            MarkScheduleDownloadedRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        LiftingSchedule schedule = loadScheduleForMutation(supervisor, scheduleId);
        ensureNotClosed(schedule);
        scheduleRepository.markDownloaded(supervisor.getSupervisor().getCode(), scheduleId, request);
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        return ScheduleActionResponse.success(
                normalizedScheduleId, ScheduleStatus.DOWNLOADED.name(), "Schedule marked as downloaded");
    }

    public ScheduleActionResponse startSchedule(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            StartScheduleRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        LiftingSchedule schedule = loadScheduleForMutation(supervisor, scheduleId);
        ensureNotClosed(schedule);

        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is already completed");
        }

        scheduleRepository.start(supervisor.getSupervisor().getCode(), scheduleId, request);
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        return ScheduleActionResponse.success(
                normalizedScheduleId, ScheduleStatus.IN_PROGRESS.name(), "Schedule started");
    }

    private LiftingSchedule loadSchedule(AuthenticatedSupervisor supervisor, String scheduleId) {
        return loadSchedule(supervisor, scheduleId, false);
    }

    private LiftingSchedule loadScheduleForMutation(AuthenticatedSupervisor supervisor, String scheduleId) {
        return loadSchedule(supervisor, scheduleId, true);
    }

    private LiftingSchedule loadSchedule(
            AuthenticatedSupervisor supervisor, String scheduleId, boolean lightweight) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        String supervisorCode = supervisor.getSupervisor().getCode();
        var found = lightweight
                ? scheduleRepository.findForActionByScheduleIdAndSupervisorId(normalizedScheduleId, supervisorCode)
                : scheduleRepository.findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisorCode);
        return found.orElseThrow(() -> new ResourceNotFoundException(
                "Schedule not found with id: "
                        + normalizedScheduleId
                        + " for supervisor "
                        + supervisorCode
                        + ". For ids containing '/' (e.g. 211342644/1.1), call GET /weighment/schedules/detail?scheduleId=... "
                        + "instead of a path segment, or URL-encode the slash as %2F."));
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (!supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }

    private void ensureNotClosed(LiftingSchedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.CLOSED) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is closed");
        }
    }

    /**
     * Wholesale: optional {@code lifting_order.delivery_status} when set, else {@code NOT_APPLICABLE}.
     * Retail: aggregate from shop orders only — do not use {@code lifting_order.delivery_status}
     * (ERP may store unrelated values). Cap at {@code PENDING} until lifting is in progress.
     */
    private static String resolveScheduleDeliveryStatus(
            LiftingSchedule schedule, String saleType, String retailAggregated) {
        if (!"RETAIL".equals(saleType)) {
            if (schedule.getDeliveryStatus() != null && !schedule.getDeliveryStatus().isBlank()) {
                return schedule.getDeliveryStatus().trim().toUpperCase(Locale.ROOT);
            }
            return retailAggregated;
        }
        if (!liftingAllowsRetailDeliveryProgress(schedule)) {
            return "PENDING";
        }
        return retailAggregated;
    }

    private static boolean liftingAllowsRetailDeliveryProgress(LiftingSchedule schedule) {
        if (schedule.getLiftingStatus() != null && !schedule.getLiftingStatus().isBlank()) {
            String normalized = schedule.getLiftingStatus().trim().toUpperCase(Locale.ROOT);
            return "IN_PROGRESS".equals(normalized) || "COMPLETED".equals(normalized);
        }
        ScheduleStatus status = schedule.getStatus();
        return status == ScheduleStatus.IN_PROGRESS || status == ScheduleStatus.COMPLETED;
    }
}
