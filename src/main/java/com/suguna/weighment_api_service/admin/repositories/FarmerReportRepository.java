package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.Projection.FarmerReportProjection;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FarmerReportRepository
        extends JpaRepository<BirdsLiftingOrderEntity, String> {

    @Query(value = """
        SELECT
            blo.FARMERCODE AS "site",
            CASE
                WHEN blo.CUSTOMERCODE IS NOT NULL THEN blo.CUSTOMERCODE || '-' || blo.CUS_NAME
                ELSE blo.CUS_NAME
            END AS "customer",
            COALESCE(dev.EMP_NAME, TO_CHAR(blo.MOBILEUSEREMPID)) AS "supervisor",
            wm.MANUAL_DC_NO AS "dcNo",
            TO_CHAR(wm.DC_DATE, 'DD/MM/YYYY') AS "startDate",
            TO_CHAR(wm.WM_START_TIME, 'HH24MI') AS "startTime",
            TO_CHAR(wm.WM_END_TIME, 'HH24MI') AS "endTime",
            wm.VEHICLE_NO AS "vehicleNo",
            blo.NO_OF_KGS AS "weight",
            blo.NO_OF_BIRDS AS "birds",
            wm.AVG_WT AS "avgWeight",
            wm.ERRORCOUNT AS "errCount",
            TO_CHAR(wm.ORDER_DATE, 'DD-MM-YY') AS "schDate"
        FROM SUG_MAI_BIRDS_WEIGHMENT wm
        INNER JOIN SUG_MAI_BIRDS_LIFTING_ORDER blo
            ON TO_CHAR(wm.SCHEDULE_NO) = TO_CHAR(blo.ERPINDENTID)
        LEFT JOIN (
            SELECT EMP_CODE,
                   MAX(EMP_NAME) KEEP (DENSE_RANK LAST ORDER BY NVL(LAST_UPDATED_DATE, CREATION_DATE)) AS EMP_NAME
            FROM SUG_MAI_DEVICEDATA
            GROUP BY EMP_CODE
        ) dev
            ON TO_CHAR(blo.MOBILEUSEREMPID) = dev.EMP_CODE
        WHERE blo.SOURCE = 'RETAIL'
          AND TRUNC(wm.DC_DATE) BETWEEN :fromDate AND :toDate
          AND (:branchCode IS NULL OR :branchCode = '' OR blo.BRANCHCODE = :branchCode)
          AND (:farmCode IS NULL OR :farmCode = '' OR blo.FARMERCODE = :farmCode)
        ORDER BY wm.DC_DATE ASC, wm.WM_START_TIME ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM SUG_MAI_BIRDS_WEIGHMENT wm
        INNER JOIN SUG_MAI_BIRDS_LIFTING_ORDER blo
            ON TO_CHAR(wm.SCHEDULE_NO) = TO_CHAR(blo.ERPINDENTID)
        WHERE blo.SOURCE = 'RETAIL'
          AND TRUNC(wm.DC_DATE) BETWEEN :fromDate AND :toDate
          AND (:branchCode IS NULL OR :branchCode = '' OR blo.BRANCHCODE = :branchCode)
          AND (:farmCode IS NULL OR :farmCode = '' OR blo.FARMERCODE = :farmCode)
        """,
            nativeQuery = true)
    Page<FarmerReportProjection> findFarmerReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("branchCode") String branchCode,
            @Param("farmCode") String farmCode,
            Pageable pageable
    );
}
