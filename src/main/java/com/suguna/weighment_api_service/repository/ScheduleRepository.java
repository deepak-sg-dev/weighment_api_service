package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ScheduleRepository {

    List<LiftingSchedule> findBySupervisorIdAndStatusIn(String supervisorId, Set<ScheduleStatus> statuses);

    Optional<LiftingSchedule> findByScheduleIdAndSupervisorId(String scheduleId, String supervisorId);

    LiftingSchedule save(LiftingSchedule schedule);
}
