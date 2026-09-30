package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScheduleRepository {

    List<LiftingSchedule> findBySupervisorIdAndStatusIn(String supervisorId, Set<ScheduleStatus> statuses);

    Optional<LiftingSchedule> findByScheduleIdAndSupervisorId(String scheduleId, String supervisorId);

    LiftingSchedule save(LiftingSchedule schedule);

    default void markDownloaded(String supervisorId, String scheduleId, MarkScheduleDownloadedRequest request) {
        LiftingSchedule schedule = findByScheduleIdAndSupervisorId(scheduleId, supervisorId)
                .orElseThrow();
        schedule.setDownloaded(true);
        if (schedule.getStatus() == ScheduleStatus.ASSIGNED) {
            schedule.setStatus(ScheduleStatus.DOWNLOADED);
        }
        save(schedule);
    }

    default void start(String supervisorId, String scheduleId, StartScheduleRequest request) {
        LiftingSchedule schedule = findByScheduleIdAndSupervisorId(scheduleId, supervisorId)
                .orElseThrow();
        schedule.setStatus(ScheduleStatus.IN_PROGRESS);
        save(schedule);
    }
}
