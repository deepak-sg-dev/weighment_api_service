package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationBranchRepository
        extends JpaRepository<OrganizationBranch, Long> {

    // Existing method used elsewhere
    List<OrganizationBranch> findByDivisionNameIgnoreCase(
            String divisionName);

    // Pagination
    Page<OrganizationBranch> findByDivisionNameIgnoreCase(
            String divisionName,
            Pageable pageable);

    // Search + Pagination
    Page<OrganizationBranch>
    findByDivisionNameIgnoreCaseAndBranchNameContainingIgnoreCase(
            String divisionName,
            String branchName,
            Pageable pageable);

    // Branch Filter + Pagination
    Page<OrganizationBranch>
    findByDivisionNameIgnoreCaseAndBranchId(
            String divisionName,
            Long branchId,
            Pageable pageable);

    // --- REGION QUERIES ---

    List<OrganizationBranch> findByDivisionNameIgnoreCaseAndRegionIdIsNotNull(
            String divisionName);

    List<OrganizationBranch> findByDivisionNameIgnoreCaseAndRegionContainingIgnoreCase(
            String divisionName,
            String region);

    List<OrganizationBranch> findByDivisionNameIgnoreCaseAndRegionId(
            String divisionName,
            Long regionId);

    // --- COMPANY CONFIGURATION QUERY ---

    @Query(value = "SELECT sug_fnd_pkg.get_reg_off_address(:orgId) FROM dual", nativeQuery = true)
    String getRegisteredOfficeAddress(@Param("orgId") Long orgId);
}
