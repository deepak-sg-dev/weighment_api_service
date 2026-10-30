package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorRepository
        extends JpaRepository<Supervisor, String> {

    List<Supervisor> findByOrganizationIdAndApplication(
            Long organizationId,
            String application);

    Optional<Supervisor> findByEmpCodeAndApplication(
            String empCode,
            String application);
}