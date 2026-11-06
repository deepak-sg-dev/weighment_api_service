package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.WeighmentWorkflowStep;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleWorkflowDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleWorkflowStepDto;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * V2 workflow steps for get-schedule. Until ERP/admin config is wired, returns the standard flow with all
 * steps enabled (matches V2 document defaults).
 */
@Service
public class ScheduleWorkflowService {

    private static final List<ScheduleWorkflowStepDto> DEFAULT_LIFTING =
            List.of(
                    step(WeighmentWorkflowStep.GEOFENCING, 1),
                    step(WeighmentWorkflowStep.BLE_SCALE_CONNECTION, 2),
                    step(WeighmentWorkflowStep.OTP_VERIFICATION, 3),
                    step(WeighmentWorkflowStep.QR_VERIFICATION, 4, false),
                    step(WeighmentWorkflowStep.VEHICLE_PHOTO, 5),
                    step(WeighmentWorkflowStep.VEHICLE_TIMELINE, 6));

    private static final List<ScheduleWorkflowStepDto> DEFAULT_DELIVERY =
            List.of(
                    step(WeighmentWorkflowStep.GEOFENCING, 1),
                    step(WeighmentWorkflowStep.BLE_SCALE_CONNECTION, 2),
                    step(WeighmentWorkflowStep.OTP_VALIDATION, 3),
                    step(WeighmentWorkflowStep.QR_VALIDATION, 4, false),
                    step(WeighmentWorkflowStep.SHOP_PHOTO, 5),
                    step(WeighmentWorkflowStep.CUSTOMER_SIGNATURE_PHOTO, 6),
                    step(WeighmentWorkflowStep.SHOP_STOCK, 7),
                    step(WeighmentWorkflowStep.VEHICLE_TIMELINE, 8));

    public ScheduleWorkflowDto resolveForSchedule(String scheduleId) {
        return ScheduleWorkflowDto.builder()
                .lifting(copy(DEFAULT_LIFTING))
                .delivery(copy(DEFAULT_DELIVERY))
                .build();
    }

    public boolean isLiftingStepEnabled(String scheduleId, WeighmentWorkflowStep step) {
        return isEnabled(resolveForSchedule(scheduleId).getLifting(), step);
    }

    public boolean isDeliveryStepEnabled(String scheduleId, WeighmentWorkflowStep step) {
        return isEnabled(resolveForSchedule(scheduleId).getDelivery(), step);
    }

    private static boolean isEnabled(List<ScheduleWorkflowStepDto> steps, WeighmentWorkflowStep step) {
        if (steps == null) {
            return true;
        }
        return steps.stream()
                .filter(s -> step.name().equals(s.getStep()))
                .findFirst()
                .map(ScheduleWorkflowStepDto::isEnabled)
                .orElse(true);
    }

    private static List<ScheduleWorkflowStepDto> copy(List<ScheduleWorkflowStepDto> source) {
        return source.stream()
                .map(s -> ScheduleWorkflowStepDto.builder()
                        .step(s.getStep())
                        .sequence(s.getSequence())
                        .enabled(s.isEnabled())
                        .build())
                .toList();
    }

    private static ScheduleWorkflowStepDto step(WeighmentWorkflowStep step, int sequence) {
        return step(step, sequence, true);
    }

    private static ScheduleWorkflowStepDto step(WeighmentWorkflowStep step, int sequence, boolean enabled) {
        return ScheduleWorkflowStepDto.builder()
                .step(step.name())
                .sequence(sequence)
                .enabled(enabled)
                .build();
    }
}
