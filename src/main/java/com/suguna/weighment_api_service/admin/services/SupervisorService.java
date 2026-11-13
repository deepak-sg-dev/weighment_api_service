package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;

public interface SupervisorService {

    SupervisorListResponseDTO getSupervisors(
            Integer page,
            Integer limit,
            String search,
            Long branchId,
            String status);

    SupervisorResponseDTO getSupervisor(
            String supervisorId);
}
