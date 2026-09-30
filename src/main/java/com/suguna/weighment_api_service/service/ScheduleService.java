package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleActionResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDetailResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.mapper.ScheduleMapper;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleService {

    private static final Set<ScheduleStatus> DEFAULT_LIST_STATUSES =
            EnumSet.of(ScheduleStatus.ASSIGNED, ScheduleStatus.IN_PROGRESS, ScheduleStatus.DOWNLOADED);

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public ScheduleListResponse getAssignedSchedules(AuthenticatedSupervisor supervisor, String statusCsv) {
        Set<ScheduleStatus> statuses = ScheduleStatus.parseCsv(statusCsv);
        if (statusCsv != null && !statusCsv.isBlank() && statuses.isEmpty()) {
            throw new BadRequestException("Invalid status filter. Allowed: assigned, in_progress, downloaded");
        }
        if (statuses.isEmpty()) {
            statuses = DEFAULT_LIST_STATUSES;
        }

        List<LiftingSchedule> schedules = scheduleRepository.findBySupervisorIdAndStatusIn(
                supervisor.getSupervisor().getCode(), statuses);

        return ScheduleListResponse.builder()
                .success(true)
                .schedules(schedules.stream().map(ScheduleMapper::toSummary).toList())
                .build();
    }

    public ScheduleDetailResponse getScheduleDetails(AuthenticatedSupervisor supervisor, String scheduleId) {
        LiftingSchedule schedule = loadSchedule(supervisor, scheduleId);
        return ScheduleDetailResponse.builder()
                .success(true)
                .schedule(ScheduleMapper.toDetail(schedule))
                .build();
    }

    public ScheduleActionResponse markDownloaded(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            MarkScheduleDownloadedRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        LiftingSchedule schedule = loadSchedule(supervisor, scheduleId);
        ensureNotClosed(schedule);
        scheduleRepository.markDownloaded(supervisor.getSupervisor().getCode(), scheduleId, request);
        LiftingSchedule updated = loadSchedule(supervisor, scheduleId);

        return ScheduleActionResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .status(updated.getStatus().name())
                .message("Schedule marked as downloaded")
                .build();
    }

    public ScheduleActionResponse startSchedule(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            StartScheduleRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        LiftingSchedule schedule = loadSchedule(supervisor, scheduleId);
        ensureNotClosed(schedule);

        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is already completed");
        }

        scheduleRepository.start(supervisor.getSupervisor().getCode(), scheduleId, request);
        LiftingSchedule updated = loadSchedule(supervisor, scheduleId);

        return ScheduleActionResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .status(updated.getStatus().name())
                .message("Schedule started")
                .build();
    }

    private LiftingSchedule loadSchedule(AuthenticatedSupervisor supervisor, String scheduleId) {
        return scheduleRepository
                .findByScheduleIdAndSupervisorId(scheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: "
                                + scheduleId
                                + " for supervisor "
                                + supervisor.getSupervisor().getCode()
                                + ". Use GET /weighment/schedules after device login, or log in as the supervisor assigned to this indent (ERP mobileuserempid)."));
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
}
