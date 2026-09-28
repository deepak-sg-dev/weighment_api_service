package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryScheduleRepository implements ScheduleRepository {

    private final Map<String, LiftingSchedule> schedules = new ConcurrentHashMap<>();

    public InMemoryScheduleRepository() {
        seedSampleSchedule();
    }

    @Override
    public List<LiftingSchedule> findBySupervisorIdAndStatusIn(String supervisorId, Set<ScheduleStatus> statuses) {
        return schedules.values().stream()
                .filter(schedule -> schedule.getSupervisorId().equals(supervisorId))
                .filter(schedule -> statuses.isEmpty() || statuses.contains(schedule.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LiftingSchedule> findByScheduleIdAndSupervisorId(String scheduleId, String supervisorId) {
        LiftingSchedule schedule = schedules.get(scheduleId);
        if (schedule == null || !schedule.getSupervisorId().equals(supervisorId)) {
            return Optional.empty();
        }
        return Optional.of(schedule);
    }

    @Override
    public LiftingSchedule save(LiftingSchedule schedule) {
        schedules.put(schedule.getScheduleId(), schedule);
        return schedule;
    }

    private void seedSampleSchedule() {
        LiftingSchedule schedule = LiftingSchedule.builder()
                .scheduleId("SCH-10001")
                .supervisorId("SUP-1024")
                .farmId("FARM-001")
                .farmName("Sample Farm")
                .farmLatitude(11.0168)
                .farmLongitude(76.9558)
                .geofenceRadiusMeters(200)
                .farmerId("FMR-001")
                .farmerName("Farmer Name")
                .farmerMobile("9XXXXXXXXX")
                .traderId("TRD-001")
                .traderName("Trader Name")
                .traderMobile("9XXXXXXXXX")
                .productId("BROILER")
                .productName("Broiler")
                .plannedQuantity(2500)
                .operationType("BROILER")
                .scaleType("HANGING")
                .status(ScheduleStatus.ASSIGNED)
                .downloaded(false)
                .standardBirdsPerCage(12)
                .stableDurationMs(2000L)
                .toleranceMin(1.8)
                .toleranceMax(2.4)
                .captureMode("AUTO")
                .completionPhotoRequired(true)
                .build();
        schedules.put(schedule.getScheduleId(), schedule);
    }
}
