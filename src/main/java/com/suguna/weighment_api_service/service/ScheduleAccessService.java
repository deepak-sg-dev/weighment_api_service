package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import com.suguna.weighment_api_service.util.VehicleRegistrationSupport;
import org.springframework.stereotype.Service;

/**
 * Resolves lifting / retail schedule access for APP supervisors and DRIVER vehicle sessions.
 */
@Service
public class ScheduleAccessService {

    private final ScheduleRepository scheduleRepository;
    private final RetailDeliveryRepository retailDeliveryRepository;

    public ScheduleAccessService(
            ScheduleRepository scheduleRepository, RetailDeliveryRepository retailDeliveryRepository) {
        this.scheduleRepository = scheduleRepository;
        this.retailDeliveryRepository = retailDeliveryRepository;
    }

    public String liftingSupervisorCode(AuthenticatedSupervisor session, String scheduleId) {
        String normalized = requireScheduleId(scheduleId);
        if (!session.isDriver()) {
            return session.getSupervisor().getCode();
        }
        LiftingSchedule lifting = requireLiftingForDriver(session, normalized);
        String emp = lifting.getSupervisorId();
        if (emp == null || emp.isBlank()) {
            throw new ResourceNotFoundException(
                    "Schedule " + normalized + " has no supervisor assigned on the lifting order.");
        }
        return emp.trim();
    }

    public LiftingSchedule requireLifting(AuthenticatedSupervisor session, String scheduleId) {
        String normalized = requireScheduleId(scheduleId);
        if (session.isDriver()) {
            return requireLiftingForDriver(session, normalized);
        }
        return scheduleRepository
                .findByScheduleIdAndSupervisorId(normalized, session.getSupervisor().getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + normalized));
    }

    public LiftingSchedule requireLiftingForAction(AuthenticatedSupervisor session, String scheduleId) {
        String normalized = requireScheduleId(scheduleId);
        if (session.isDriver()) {
            return requireLiftingForDriver(session, normalized);
        }
        return scheduleRepository
                .findForActionByScheduleIdAndSupervisorId(normalized, session.getSupervisor().getCode())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Schedule not found: " + normalized));
    }

    public RetailScheduleContext loadRetailSchedule(AuthenticatedSupervisor session, String scheduleId) {
        String normalized = requireScheduleId(scheduleId);
        return retailDeliveryRepository.loadRetailSchedule(
                liftingSupervisorCode(session, normalized), normalized);
    }

    private LiftingSchedule requireLiftingForDriver(AuthenticatedSupervisor session, String normalizedScheduleId) {
        String plate = VehicleRegistrationSupport.normalizeOrNull(session.getVehicleNo());
        if (plate == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Driver token is missing vehicleNo. Login again.");
        }
        return scheduleRepository
                .findByScheduleIdAndVehicleNo(normalizedScheduleId, plate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + normalizedScheduleId + " for vehicle " + plate));
    }

    private static String requireScheduleId(String scheduleId) {
        if (scheduleId == null || scheduleId.isBlank()) {
            throw new BadRequestException("scheduleId is required");
        }
        return ScheduleIdSupport.normalize(scheduleId);
    }
}
