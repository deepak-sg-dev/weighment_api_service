package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.UpdateScheduleVehicleRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.UpdateScheduleVehicleResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/**
 * Updates vehicle/driver on {@code sug_mai_birds_lifting_order} (one row per schedule today).
 *
 * <p>Do not gate on {@code lifting_status} / {@code delivery_status} {@code COMPLETED} — lifting may
 * finish while delivery continues, and a later phase may use one vehicle at farm and different vehicles
 * per shop (per-delivery API + columns, not schedule-level COMPLETED).
 */
@Service
public class ScheduleVehicleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleVehicleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public UpdateScheduleVehicleResponse updateVehicle(
            AuthenticatedSupervisor supervisor, String scheduleId, UpdateScheduleVehicleRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        LiftingSchedule schedule = loadScheduleForUpdate(supervisor, scheduleId);
        ensureMutable(schedule);
        UpdateScheduleVehicleRequest.VehiclePayload vehicle = request.getVehicle();
        String normalized = ScheduleIdSupport.normalize(scheduleId);
        String supervisorCode = supervisor.getSupervisor().getCode();
        scheduleRepository.updateVehicle(
                supervisorCode,
                normalized,
                vehicle.getNumber().trim().toUpperCase(),
                vehicle.getDriverName().trim(),
                vehicle.getDriverMobile().trim(),
                supervisorCode);
        ScheduleVehicleDto dto = new ScheduleVehicleDto();
        dto.setNumber(vehicle.getNumber().trim().toUpperCase());
        dto.setDriverName(vehicle.getDriverName().trim());
        dto.setDriverMobile(vehicle.getDriverMobile().trim());
        return UpdateScheduleVehicleResponse.builder()
                .success(true)
                .message("Vehicle updated")
                .scheduleId(normalized)
                .vehicle(dto)
                .updatedAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.now().atOffset(ZoneOffset.ofHoursMinutes(5, 30))))
                .build();
    }

    /** Only hard stops: cancelled indent or closed schedule — not lifting/delivery {@code COMPLETED}. */
    private void ensureMutable(LiftingSchedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.CLOSED) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is closed");
        }
        if (schedule.getLiftingStatus() != null
                && "CANCELLED".equalsIgnoreCase(schedule.getLiftingStatus().trim())) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is cancelled");
        }
    }

    private LiftingSchedule loadScheduleForUpdate(AuthenticatedSupervisor supervisor, String scheduleId) {
        String normalized = ScheduleIdSupport.normalize(scheduleId);
        return scheduleRepository
                .findForActionByScheduleIdAndSupervisorId(normalized, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", normalized));
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (!supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }
}
