package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationBranchRepository
        extends JpaRepository<OrganizationBranch, Long> {

    List<OrganizationBranch> findByDivisionNameIgnoreCase(
            String divisionName);
}