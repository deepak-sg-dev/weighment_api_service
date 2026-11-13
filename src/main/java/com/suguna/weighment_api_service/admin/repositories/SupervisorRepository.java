package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Supervisor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorRepository
        extends JpaRepository<Supervisor, String>,
        JpaSpecificationExecutor<Supervisor> {

    // Existing method - keep for compatibility
    List<Supervisor> findByOrganizationIdAndApplication(
            Long organizationId,
            String application);

    // Pagination
    Page<Supervisor> findByOrganizationIdAndApplication(
            Long organizationId,
            String application,
            Pageable pageable);

    // Search + Pagination
    Page<Supervisor>
    findByOrganizationIdAndApplicationAndEmpNameContainingIgnoreCase(
            Long organizationId,
            String application,
            String empName,
            Pageable pageable);

    Optional<Supervisor> findByEmpCodeAndApplication(
            String empCode,
            String application);
}
