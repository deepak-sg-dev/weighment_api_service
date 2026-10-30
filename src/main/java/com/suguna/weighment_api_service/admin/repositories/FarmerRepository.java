package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmerRepository
        extends JpaRepository<Farmer, String> {

    List<Farmer> findByBranchCode(
            String branchCode);
}