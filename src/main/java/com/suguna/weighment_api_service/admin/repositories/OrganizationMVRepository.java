package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.OrganizationMV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationMVRepository
        extends JpaRepository<OrganizationMV, Long> {

    Optional<OrganizationMV> findByBranchId(Long branchId);
}
