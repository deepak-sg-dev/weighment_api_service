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

    /** RETAIL: trader signature optional at lifting (delivery flow captures shop signatures). */
    private static final List<ScheduleWorkflowStepDto> DEFAULT_LIFTING_RETAIL =
            List.of(
                    step(WeighmentWorkflowStep.GEOFENCING, 1),
                    step(WeighmentWorkflowStep.BLE_SCALE_CONNECTION, 2),
                    step(WeighmentWorkflowStep.OTP_VERIFICATION, 3),
                    step(WeighmentWorkflowStep.QR_VERIFICATION, 4, false),
                    step(WeighmentWorkflowStep.VEHICLE_PHOTO, 5),
                    step(WeighmentWorkflowStep.FARMER_SIGNATURE_PHOTO, 6),
                    step(WeighmentWorkflowStep.LIFTING_SUPERVISOR_SIGNATURE_PHOTO, 7),
                    step(WeighmentWorkflowStep.TRADER_SIGNATURE_PHOTO, 8, false),
                    step(WeighmentWorkflowStep.VEHICLE_TIMELINE, 9));

    /** WHOLESALE: require trader + lifting supervisor signatures at lifting (no retail delivery stops). */
    private static final List<ScheduleWorkflowStepDto> DEFAULT_LIFTING_WHOLESALE =
            List.of(
                    step(WeighmentWorkflowStep.GEOFENCING, 1),
                    step(WeighmentWorkflowStep.BLE_SCALE_CONNECTION, 2),
                    step(WeighmentWorkflowStep.OTP_VERIFICATION, 3),
                    step(WeighmentWorkflowStep.QR_VERIFICATION, 4, false),
                    step(WeighmentWorkflowStep.VEHICLE_PHOTO, 5),
                    step(WeighmentWorkflowStep.FARMER_SIGNATURE_PHOTO, 6),
                    step(WeighmentWorkflowStep.LIFTING_SUPERVISOR_SIGNATURE_PHOTO, 7),
                    step(WeighmentWorkflowStep.TRADER_SIGNATURE_PHOTO, 8),
                    step(WeighmentWorkflowStep.VEHICLE_TIMELINE, 9));

    private static final List<ScheduleWorkflowStepDto> DEFAULT_DELIVERY =
            List.of(
                    step(WeighmentWorkflowStep.GEOFENCING, 1),
                    step(WeighmentWorkflowStep.BLE_SCALE_CONNECTION, 2),
                    step(WeighmentWorkflowStep.OTP_VALIDATION, 3),
                    step(WeighmentWorkflowStep.QR_VALIDATION, 4, false),
                    step(WeighmentWorkflowStep.SHOP_PHOTO, 5),
                    step(WeighmentWorkflowStep.CUSTOMER_SIGNATURE_PHOTO, 6),
                    step(WeighmentWorkflowStep.DELIVERY_SUPERVISOR_SIGNATURE_PHOTO, 7),
                    step(WeighmentWorkflowStep.SHOP_STOCK, 8),
                    step(WeighmentWorkflowStep.VEHICLE_TIMELINE, 9));

    /**
     * Default workflow when sale type is unknown (e.g. legacy callers): same as retail lifting list.
     */
    public ScheduleWorkflowDto resolveForSchedule(String scheduleId) {
        return resolveForSaleType(null);
    }

    /**
     * Lifting steps depend on sale type: wholesale enables trader signature at the farm; retail keeps it optional.
     */
    public ScheduleWorkflowDto resolveForSaleType(String saleType) {
        List<ScheduleWorkflowStepDto> lifting =
                isWholesaleSaleType(saleType) ? copy(DEFAULT_LIFTING_WHOLESALE) : copy(DEFAULT_LIFTING_RETAIL);
        return ScheduleWorkflowDto.builder()
                .lifting(lifting)
                .delivery(copy(DEFAULT_DELIVERY))
                .build();
    }

    public boolean isLiftingStepEnabled(String scheduleId, WeighmentWorkflowStep step) {
        return isEnabled(resolveForSchedule(scheduleId).getLifting(), step);
    }

    public boolean isLiftingStepEnabled(String saleType, String scheduleId, WeighmentWorkflowStep step) {
        return isEnabled(resolveForSaleType(saleType).getLifting(), step);
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

    private static boolean isWholesaleSaleType(String saleType) {
        return saleType != null && "WHOLESALE".equalsIgnoreCase(saleType.trim());
    }
}
