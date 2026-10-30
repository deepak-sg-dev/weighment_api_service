package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraderRepository
        extends JpaRepository<Trader, Long> {

    List<Trader> findByBranchCode(
            String branchCode);
}