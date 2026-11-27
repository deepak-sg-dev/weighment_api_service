package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity_1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailScheduleReport_1Repository
        extends JpaRepository<BirdsLiftingOrderEntity_1, String>,
        JpaSpecificationExecutor<BirdsLiftingOrderEntity_1> {
}
