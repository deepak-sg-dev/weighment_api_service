package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps {@code SUG.SUG_FARM_MST_HEADER}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugFarmMstHeader.SugFarmMstHeaderId.class)
@Entity
@Table(name = "SUG_FARM_MST_HEADER", schema = "SUG")
public class SugFarmMstHeader {

    @Id
    @Column(name = "FARM_ID")
    private BigDecimal farmId;

    @Id
    @Column(name = "FARM_CODE")
    private String farmCode;

    @Id
    @Column(name = "FARM_NAME_VILLAGE")
    private String farmNameVillage;

    @Column(name = "REGION_CODE")
    private String regionCode;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "SUB_BRANCH_CODE")
    private String subBranchCode;

    @Column(name = "UNLOADING_POINT")
    private String unloadingPoint;

    @Column(name = "LINE_CODE")
    private String lineCode;

    @Column(name = "AREA_CODE")
    private String areaCode;

    @Column(name = "DIVISION")
    private String division;

    @Column(name = "FARM_TYPE")
    private String farmType;

    @Column(name = "FARMER_NAME")
    private String farmerName;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDateTime dateOfBirth;

    @Column(name = "EDU_QUALIFICATION")
    private String eduQualification;

    @Column(name = "CONTACT_PERSON")
    private String contactPerson;

    @Column(name = "REARING_AREA")
    private BigDecimal rearingArea;

    @Column(name = "FEED_ROOM_CAPACITY")
    private BigDecimal feedRoomCapacity;

    @Column(name = "FARM_STATUS")
    private String farmStatus;

    @Column(name = "FARM_STATUS_DATE")
    private LocalDateTime farmStatusDate;

    @Column(name = "REASON_FOR_CANCELLATION")
    private String reasonForCancellation;

    @Column(name = "TOTAL_AREA")
    private BigDecimal totalArea;

    @Column(name = "TOTAL_REARING_AREA")
    private BigDecimal totalRearingArea;

    @Column(name = "CREATED_BY")
    private BigDecimal createdBy;

    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "LAST_UPDATED_BY")
    private BigDecimal lastUpdatedBy;

    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private LocalDateTime lastUpdateDate;

    @Column(name = "LAST_UPDATE_LOGIN")
    private BigDecimal lastUpdateLogin;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "SPOUSE_NAME")
    private String spouseName;

    @Column(name = "SPOUSE_DOB")
    private LocalDateTime spouseDob;

    @Column(name = "WEDDING_DATE")
    private LocalDateTime weddingDate;

    @Column(name = "BRANCH_ID", nullable = false)
    private BigDecimal branchId;

    @Column(name = "PARENT_FARM_CODE")
    private String parentFarmCode;

    @Column(name = "PARENT_REGION_CODE")
    private String parentRegionCode;

    @Column(name = "PAN_NUMBER")
    private String panNumber;

    @Column(name = "EDU_QUALIFICATION_GROUP")
    private String eduQualificationGroup;

    @Column(name = "TAX_NAME")
    private String taxName;

    @Column(name = "SECTION_CODE")
    private String sectionCode;

    @Column(name = "TAX_ID")
    private BigDecimal taxId;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "LATITUDE")
    private BigDecimal latitude;

    @Column(name = "LONGITUDE")
    private BigDecimal longitude;

    @Column(name = "POSTED_FLAG")
    private String postedFlag;

    @Column(name = "WF_STATUS")
    private String wfStatus;

    @Column(name = "ITEM_KEY")
    private BigDecimal itemKey;

    @Column(name = "DECLARATION_STATUS")
    private String declarationStatus;

    @Column(name = "CREATED_EMP_ID")
    private BigDecimal createdEmpId;

    @Column(name = "NEW_FM_DEC_STATUS")
    private String newFmDecStatus;

    @Column(name = "NID")
    private String nid;

    @Column(name = "KYC_DATE")
    private LocalDateTime kycDate;

    @Column(name = "FAMILY_FLAG")
    private String familyFlag;

    @Column(name = "ENQ_REF_NUMBER")
    private String enqRefNumber;

    @Column(name = "DOC_SOURCE")
    private String docSource;

    @Column(name = "FARMER_LIVING_LOC")
    private String farmerLivingLoc;

    @Column(name = "DISTANCE_FROM_FARM")
    private String distanceFromFarm;

    @Column(name = "FARMER_ADD_PROFESSION")
    private String farmerAddProfession;

    @Column(name = "FARMER_PRIOR_PERFORM")
    private String farmerPriorPerform;

    @Column(name = "INCOME_DEPENDANCY")
    private String incomeDependancy;

    @Column(name = "AADHAR_NO")
    private String aadharNo;

    @Column(name = "REASON_FOR_REACTIVE")
    private String reasonForReactive;

    @Column(name = "LEGAL_ACTION")
    private String legalAction;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SugFarmMstHeaderId implements java.io.Serializable {
        private BigDecimal farmId;
        private String farmCode;
        private String farmNameVillage;
    }
}
