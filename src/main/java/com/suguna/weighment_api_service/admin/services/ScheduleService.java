package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.ScheduleListResponseDTO;

public interface ScheduleService {

    ScheduleListResponseDTO getSchedules(
            Integer page,
            Integer limit,
            String search,
            Long branchId,
            String supervisorId,
            String traderId,
            String vehicleId,
            String status);
}