package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.Projection.DailyReportProjection;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyReportRepository
        extends JpaRepository<BirdsLiftingOrderEntity, String> {

    @Query(value = """
        SELECT DISTINCT
            blo.ERPINDENTID AS "schCode",
            COALESCE(blo.FARMERNAME, wm.FARM_NAME) AS "farmerName",
            COALESCE(blo.FARMERCODE, wm.FARM_CODE) AS "farmCode",
            COALESCE(hp.PARTY_NAME, blo.CUS_NAME) AS "customerName",
            COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE) AS "customerCode",
            wm.WM_SUPERVISOR_CODE AS "supervisorCode",
            COALESCE(wm.WM_SUPERVISOR_NAME, dev.EMP_NAME) AS "supervisorName",
            TO_CHAR(wm.REF_ID) AS "dcNumber",
            NULL AS "slipNo",
            TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') AS "weighmentStartDate",
            TO_CHAR(wm.WM_START_TIME, 'HH24:MI:SS') AS "weighmentStartTime",
            TO_CHAR(wm.WM_END_TIME, 'HH24:MI:SS') AS "weighmentEndTime",
            COALESCE(blo.VEHICLENO, wm.VEHICLE_NO) AS "vehicleNo",
            blo.NO_OF_KGS AS "weightOfBirds",
            blo.NO_OF_BIRDS AS "noOfBirds",
            wm.BIRD_TYPE AS "birdType",
            wm.AVG_WT AS "avgWeight",
            wm.ERRORCOUNT AS "errorCount",
            wm.ABORTCOUNT AS "abortCount",
            wm.FEED_STOCK_BAGS AS "feedStockBag",
            TO_CHAR(wm.VEHICLE_INTIME, 'DD-MM-YYYY HH24:MI:SS') AS "vehicleIn",
            TO_CHAR(wm.VEHICLE_OUTTIME, 'DD-MM-YYYY HH24:MI:SS') AS "vehicleOut",
            wm.BRANCH_CODE AS "branchCode",
            TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') AS "schDate",
            TO_CHAR(rod.DELV_START_DT, 'DD-MM-YYYY HH24:MI:SS') AS "deliveryStartTime",
            TO_CHAR(rod.DELV_END_DT, 'DD-MM-YYYY HH24:MI:SS') AS "deliveryEndTime"
        FROM SUG_MAI_BIRDS_WEIGHMENT wm
        INNER JOIN SUG_MAI_BIRDS_LIFTING_ORDER blo
            ON TO_CHAR(wm.SCHEDULE_NO) = blo.ERPINDENTID
        LEFT JOIN (
            SELECT EMP_CODE,
                   MAX(EMP_NAME) KEEP (DENSE_RANK LAST ORDER BY NVL(LAST_UPDATED_DATE, CREATION_DATE)) AS EMP_NAME
            FROM SUG_MAI_DEVICEDATA
            GROUP BY EMP_CODE
        ) dev
            ON wm.WM_SUPERVISOR_CODE = dev.EMP_CODE
        LEFT JOIN OE_ORDER_LINES_ALL oola
            ON blo.ERPINDENTID = oola.ATTRIBUTE2
        LEFT JOIN OE_ORDER_HEADERS_ALL ooha
            ON oola.HEADER_ID = ooha.HEADER_ID
        LEFT JOIN PO_REQUISITION_HEADERS_ALL prha
            ON ooha.SOURCE_DOCUMENT_ID = prha.REQUISITION_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_HDR roh
            ON prha.REQUISITION_HEADER_ID = roh.REQ_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_DTL rod
            ON roh.HEADER_ID = rod.HEADER_ID
        LEFT JOIN HZ_CUST_ACCOUNTS hca
            ON COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE) = hca.ACCOUNT_NUMBER
        LEFT JOIN HZ_PARTIES hp
            ON hca.PARTY_ID = hp.PARTY_ID
        WHERE blo.SOURCE = 'RETAIL'
          AND TRUNC(wm.DC_DATE) BETWEEN :fromDate AND :toDate
          AND (:branchCode IS NULL OR :branchCode = '' OR wm.BRANCH_CODE = :branchCode)
          AND (:farmCode IS NULL OR :farmCode = '' OR COALESCE(blo.FARMERCODE, wm.FARM_CODE) = :farmCode)
        ORDER BY TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') ASC
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT
                blo.ERPINDENTID,
                COALESCE(blo.FARMERNAME, wm.FARM_NAME),
                COALESCE(blo.FARMERCODE, wm.FARM_CODE),
                COALESCE(hp.PARTY_NAME, blo.CUS_NAME),
                COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE),
                wm.WM_SUPERVISOR_CODE,
                COALESCE(wm.WM_SUPERVISOR_NAME, dev.EMP_NAME),
                wm.REF_ID,
                wm.DC_DATE,
                wm.WM_START_TIME,
                wm.WM_END_TIME,
                COALESCE(blo.VEHICLENO, wm.VEHICLE_NO),
                blo.NO_OF_KGS,
                blo.NO_OF_BIRDS,
                wm.BIRD_TYPE,
                wm.AVG_WT,
                wm.ERRORCOUNT,
                wm.ABORTCOUNT,
                wm.FEED_STOCK_BAGS,
                wm.VEHICLE_INTIME,
                wm.VEHICLE_OUTTIME,
                wm.BRANCH_CODE,
                rod.DELV_START_DT,
                rod.DELV_END_DT
            FROM SUG_MAI_BIRDS_WEIGHMENT wm
            INNER JOIN SUG_MAI_BIRDS_LIFTING_ORDER blo
                ON TO_CHAR(wm.SCHEDULE_NO) = blo.ERPINDENTID
            LEFT JOIN (
                SELECT EMP_CODE,
                       MAX(EMP_NAME) KEEP (DENSE_RANK LAST ORDER BY NVL(LAST_UPDATED_DATE, CREATION_DATE)) AS EMP_NAME
                FROM SUG_MAI_DEVICEDATA
                GROUP BY EMP_CODE
            ) dev
                ON wm.WM_SUPERVISOR_CODE = dev.EMP_CODE
            LEFT JOIN OE_ORDER_LINES_ALL oola
                ON blo.ERPINDENTID = oola.ATTRIBUTE2
            LEFT JOIN OE_ORDER_HEADERS_ALL ooha
                ON oola.HEADER_ID = ooha.HEADER_ID
            LEFT JOIN PO_REQUISITION_HEADERS_ALL prha
                ON ooha.SOURCE_DOCUMENT_ID = prha.REQUISITION_HEADER_ID
            LEFT JOIN SUG_MAI_RETAIL_ORDER_HDR roh
                ON prha.REQUISITION_HEADER_ID = roh.REQ_HEADER_ID
            LEFT JOIN SUG_MAI_RETAIL_ORDER_DTL rod
                ON roh.HEADER_ID = rod.HEADER_ID
            LEFT JOIN HZ_CUST_ACCOUNTS hca
                ON COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE) = hca.ACCOUNT_NUMBER
            LEFT JOIN HZ_PARTIES hp
                ON hca.PARTY_ID = hp.PARTY_ID
            WHERE blo.SOURCE = 'RETAIL'
              AND TRUNC(wm.DC_DATE) BETWEEN :fromDate AND :toDate
              AND (:branchCode IS NULL OR :branchCode = '' OR wm.BRANCH_CODE = :branchCode)
              AND (:farmCode IS NULL OR :farmCode = '' OR COALESCE(blo.FARMERCODE, wm.FARM_CODE) = :farmCode)
        )
        """,
            nativeQuery = true)
    Page<DailyReportProjection> findDailyReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("branchCode") String branchCode,
            @Param("farmCode") String farmCode,
            Pageable pageable
    );

    @Query(value = """
        SELECT DISTINCT
            blo.ERPINDENTID AS "schCode",
            COALESCE(blo.FARMERNAME, wm.FARM_NAME) AS "farmerName",
            COALESCE(blo.FARMERCODE, wm.FARM_CODE) AS "farmCode",
            COALESCE(hp.PARTY_NAME, blo.CUS_NAME) AS "customerName",
            COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE) AS "customerCode",
            wm.WM_SUPERVISOR_CODE AS "supervisorCode",
            COALESCE(wm.WM_SUPERVISOR_NAME, dev.EMP_NAME) AS "supervisorName",
            TO_CHAR(wm.REF_ID) AS "dcNumber",
            NULL AS "slipNo",
            TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') AS "weighmentStartDate",
            TO_CHAR(wm.WM_START_TIME, 'HH24:MI:SS') AS "weighmentStartTime",
            TO_CHAR(wm.WM_END_TIME, 'HH24:MI:SS') AS "weighmentEndTime",
            COALESCE(blo.VEHICLENO, wm.VEHICLE_NO) AS "vehicleNo",
            blo.NO_OF_KGS AS "weightOfBirds",
            blo.NO_OF_BIRDS AS "noOfBirds",
            wm.BIRD_TYPE AS "birdType",
            wm.AVG_WT AS "avgWeight",
            wm.ERRORCOUNT AS "errorCount",
            wm.ABORTCOUNT AS "abortCount",
            wm.FEED_STOCK_BAGS AS "feedStockBag",
            TO_CHAR(wm.VEHICLE_INTIME, 'DD-MM-YYYY HH24:MI:SS') AS "vehicleIn",
            TO_CHAR(wm.VEHICLE_OUTTIME, 'DD-MM-YYYY HH24:MI:SS') AS "vehicleOut",
            wm.BRANCH_CODE AS "branchCode",
            TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') AS "schDate",
            TO_CHAR(rod.DELV_START_DT, 'DD-MM-YYYY HH24:MI:SS') AS "deliveryStartTime",
            TO_CHAR(rod.DELV_END_DT, 'DD-MM-YYYY HH24:MI:SS') AS "deliveryEndTime"
        FROM SUG_MAI_BIRDS_WEIGHMENT wm
        INNER JOIN SUG_MAI_BIRDS_LIFTING_ORDER blo
            ON TO_CHAR(wm.SCHEDULE_NO) = blo.ERPINDENTID
        LEFT JOIN (
            SELECT EMP_CODE,
                   MAX(EMP_NAME) KEEP (DENSE_RANK LAST ORDER BY NVL(LAST_UPDATED_DATE, CREATION_DATE)) AS EMP_NAME
            FROM SUG_MAI_DEVICEDATA
            GROUP BY EMP_CODE
        ) dev
            ON wm.WM_SUPERVISOR_CODE = dev.EMP_CODE
        LEFT JOIN OE_ORDER_LINES_ALL oola
            ON blo.ERPINDENTID = oola.ATTRIBUTE2
        LEFT JOIN OE_ORDER_HEADERS_ALL ooha
            ON oola.HEADER_ID = ooha.HEADER_ID
        LEFT JOIN PO_REQUISITION_HEADERS_ALL prha
            ON ooha.SOURCE_DOCUMENT_ID = prha.REQUISITION_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_HDR roh
            ON prha.REQUISITION_HEADER_ID = roh.REQ_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_DTL rod
            ON roh.HEADER_ID = rod.HEADER_ID
        LEFT JOIN HZ_CUST_ACCOUNTS hca
            ON COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE) = hca.ACCOUNT_NUMBER
        LEFT JOIN HZ_PARTIES hp
            ON hca.PARTY_ID = hp.PARTY_ID
        WHERE blo.SOURCE = 'RETAIL'
          AND TRUNC(wm.DC_DATE) BETWEEN :fromDate AND :toDate
          AND (:branchCode IS NULL OR :branchCode = '' OR wm.BRANCH_CODE = :branchCode)
          AND (:farmCode IS NULL OR :farmCode = '' OR COALESCE(blo.FARMERCODE, wm.FARM_CODE) = :farmCode)
        ORDER BY TO_CHAR(wm.DC_DATE, 'DD-MM-YYYY') ASC
        """,
            nativeQuery = true)
    List<DailyReportProjection> findAllDailyReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("branchCode") String branchCode,
            @Param("farmCode") String farmCode
    );
}
