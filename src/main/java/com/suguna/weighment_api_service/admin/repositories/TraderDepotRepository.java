package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.TraderDepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraderDepotRepository
        extends JpaRepository<TraderDepot, Long> {

    List<TraderDepot> findByCustomerId(
            Long customerId);
}