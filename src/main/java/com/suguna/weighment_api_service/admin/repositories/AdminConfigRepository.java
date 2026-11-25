package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.AdminConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfigEntity, Long> {

    Optional<AdminConfigEntity> findFirstByApplicationAndRegionIdIsNullAndBranchIdIsNullAndActiveOrderByIdDesc(
            String application,
            String active
    );

    Optional<AdminConfigEntity> findFirstByApplicationAndRegionIdAndBranchIdIsNullAndActiveOrderByIdDesc(
            String application,
            Long regionId,
            String active
    );

    Optional<AdminConfigEntity> findFirstByApplicationAndBranchIdAndActiveOrderByIdDesc(
            String application,
            Long branchId,
            String active
    );

    Optional<AdminConfigEntity> findTopByOrderByIdDesc();
}
