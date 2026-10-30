package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;

import java.util.List;

public interface SupervisorService {

    List<SupervisorResponseDTO> getSupervisors(
            Long branchId);

    SupervisorResponseDTO getSupervisor(
            String supervisorId);
}