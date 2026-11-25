package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.Projection.RetailScheduleReportProjection;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetailScheduleReportRepository
        extends JpaRepository<BirdsLiftingOrderEntity, String>,
        JpaSpecificationExecutor<BirdsLiftingOrderEntity> {

    @Query(value = """
        SELECT DISTINCT
            blo.ERPINDENTID AS "erpSchCode",
            ob.BRANCH_NAME AS "branchName",
            dev.EMP_NAME AS "supervisor",
            COALESCE(rod.CUSTOMER_CODE, TO_CHAR(blo.CUSTOMERCODE)) AS "customerCode",
            COALESCE(hp.PARTY_NAME, blo.CUS_NAME) AS "customerName",
            TRIM(
                NVL(hl.ADDRESS1, '') ||
                CASE WHEN hl.ADDRESS2 IS NOT NULL THEN ', ' || hl.ADDRESS2 ELSE '' END ||
                CASE WHEN hl.CITY IS NOT NULL THEN ', ' || hl.CITY ELSE '' END
            ) AS "customerAddress",
            rod.WEIGHT AS "weight",
            rod.QUANTITY AS "quantity",
            rod.DELV_STATUS AS "deliveryStatus",
            TO_CHAR(rod.DELV_END_DT, 'DD-MM-YYYY HH24:MI') AS "deliveryDate"
        FROM SUG_MAI_BIRDS_LIFTING_ORDER blo
        LEFT JOIN (
            SELECT BRANCH_CODE, MAX(BRANCH_NAME) AS BRANCH_NAME
            FROM SUG_ORGANIZATION_MV
            GROUP BY BRANCH_CODE
        ) ob
            ON blo.BRANCHCODE = ob.BRANCH_CODE
        LEFT JOIN (
            SELECT EMP_CODE,
                   MAX(EMP_NAME) KEEP (DENSE_RANK LAST ORDER BY NVL(LAST_UPDATED_DATE, CREATION_DATE)) AS EMP_NAME
            FROM SUG_MAI_DEVICEDATA
            GROUP BY EMP_CODE
        ) dev
            ON TO_CHAR(blo.MOBILEUSEREMPID) = dev.EMP_CODE
        LEFT JOIN OE_ORDER_LINES_ALL oola
            ON TO_CHAR(blo.ERPINDENTID) = oola.ATTRIBUTE2
        LEFT JOIN OE_ORDER_HEADERS_ALL ooha
            ON oola.HEADER_ID = ooha.HEADER_ID
        LEFT JOIN PO_REQUISITION_HEADERS_ALL prha
            ON ooha.SOURCE_DOCUMENT_ID = prha.REQUISITION_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_HDR roh
            ON prha.REQUISITION_HEADER_ID = roh.REQ_HEADER_ID
        LEFT JOIN SUG_MAI_RETAIL_ORDER_DTL rod
            ON roh.HEADER_ID = rod.HEADER_ID
        LEFT JOIN HZ_CUST_ACCOUNTS hca
            ON COALESCE(rod.CUSTOMER_CODE, TO_CHAR(blo.CUSTOMERCODE)) = hca.ACCOUNT_NUMBER
        LEFT JOIN HZ_PARTIES hp
            ON hca.PARTY_ID = hp.PARTY_ID
        LEFT JOIN HZ_CUST_ACCT_SITES_ALL hcasa
            ON hca.CUST_ACCOUNT_ID = hcasa.CUST_ACCOUNT_ID
            AND hcasa.STATUS = 'A'
        LEFT JOIN HZ_PARTY_SITES hps
            ON hcasa.PARTY_SITE_ID = hps.PARTY_SITE_ID
            AND hps.IDENTIFYING_ADDRESS_FLAG = 'Y'
        LEFT JOIN HZ_LOCATIONS hl
            ON hps.LOCATION_ID = hl.LOCATION_ID
        WHERE blo.ERPINDENTID IN (:erpIndentIds)
        """, nativeQuery = true)
    List<RetailScheduleReportProjection> findRetailDetailsByErpIndentIds(
            @Param("erpIndentIds") List<String> erpIndentIds
    );
}
