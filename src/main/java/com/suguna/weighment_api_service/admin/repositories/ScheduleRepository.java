package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.entity.SugMaiBirdsLiftingOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ScheduleRepository
        extends JpaRepository<
        SugMaiBirdsLiftingOrder,
        String>,
        JpaSpecificationExecutor<
                SugMaiBirdsLiftingOrder> {

    // Default Pagination

    Page<SugMaiBirdsLiftingOrder>
    findByApplication(
            String application,
            Pageable pageable);

    // Branch Filter

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndBranchid(
            String application,
            BigDecimal branchid,
            Pageable pageable);

    // Supervisor Filter

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndMobileuserempid(
            String application,
            String mobileuserempid,
            Pageable pageable);

    // Trader Filter

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndCustomercode(
            String application,
            String customercode,
            Pageable pageable);

    // Vehicle Filter

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndVehiclenoContainingIgnoreCase(
            String application,
            String vehicleno,
            Pageable pageable);

    // Search By Farmer

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndFarmernameContainingIgnoreCase(
            String application,
            String farmername,
            Pageable pageable);

    // Search By ERP Schedule Id

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndErpindentidContainingIgnoreCase(
            String application,
            String erpindentid,
            Pageable pageable);

    // Search By Customer Name

    Page<SugMaiBirdsLiftingOrder>
    findByApplicationAndCusNameContainingIgnoreCase(
            String application,
            String cusName,
            Pageable pageable);
}