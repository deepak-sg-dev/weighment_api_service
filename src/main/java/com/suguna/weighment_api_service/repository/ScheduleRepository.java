package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleListFilter;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScheduleRepository {

    List<LiftingSchedule> findBySupervisorIdAndStatusIn(
            String supervisorId,
            Set<ScheduleStatus> statuses,
            ScheduleListFilter listFilter,
            boolean includeAllStatuses);

    Optional<LiftingSchedule> findByScheduleIdAndSupervisorId(String scheduleId, String supervisorId);

    /** Lighter read for start/download (no GPS join). */
    Optional<LiftingSchedule> findForActionByScheduleIdAndSupervisorId(String scheduleId, String supervisorId);

    LiftingSchedule save(LiftingSchedule schedule);

    void markDownloaded(String supervisorId, String scheduleId, MarkScheduleDownloadedRequest request);

    void start(String supervisorId, String scheduleId, StartScheduleRequest request);
}
