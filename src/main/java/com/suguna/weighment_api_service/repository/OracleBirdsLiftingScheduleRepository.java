package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.integration.oracle.LiftingScheduleSqlClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class OracleBirdsLiftingScheduleRepository implements ScheduleRepository {

    private final LiftingScheduleSqlClient scheduleSqlClient;

    public OracleBirdsLiftingScheduleRepository(LiftingScheduleSqlClient scheduleSqlClient) {
        this.scheduleSqlClient = scheduleSqlClient;
    }

    @Override
    public List<LiftingSchedule> findBySupervisorIdAndStatusIn(String supervisorId, Set<ScheduleStatus> statuses) {
        return scheduleSqlClient.listSchedules(supervisorId, statuses);
    }

    @Override
    public Optional<LiftingSchedule> findByScheduleIdAndSupervisorId(String scheduleId, String supervisorId) {
        return Optional.ofNullable(scheduleSqlClient.getScheduleDetails(supervisorId, scheduleId));
    }

    @Override
    public LiftingSchedule save(LiftingSchedule schedule) {
        throw new UnsupportedOperationException("Schedule persistence is handled via Oracle SQL/package operations");
    }

    @Override
    public void markDownloaded(String supervisorId, String scheduleId, MarkScheduleDownloadedRequest request) {
        scheduleSqlClient.markScheduleDownloaded(supervisorId, scheduleId, request);
    }

    @Override
    public void start(String supervisorId, String scheduleId, StartScheduleRequest request) {
        scheduleSqlClient.startSchedule(supervisorId, scheduleId, request);
    }
}
