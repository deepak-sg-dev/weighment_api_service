package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Immutable;

/**
 * Maps {@code APPS.SUG_HR_EMP_MST_MV}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Immutable
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_HR_EMP_MST_MV", schema = "APPS")
public class SugHrEmpMstMv {

    @Column(name = "BUSINESS_GROUP_ID", nullable = false)
    private Long businessGroupId;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @Id
    @Column(name = "ASSIGNMENT_ID")
    private Long assignmentId;

    @Column(name = "SUPERVISOR_ASSIGNMENT_ID")
    private Long supervisorAssignmentId;

    @Column(name = "EMP_NO")
    private String empNo;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAMES")
    private String middleNames;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PAYROLL")
    private String payroll;

    @Column(name = "DOB")
    private LocalDateTime dob;

    @Column(name = "TOWN_OF_BIRTH")
    private String townOfBirth;

    @Column(name = "REGION_OF_BIRTH")
    private String regionOfBirth;

    @Column(name = "NO_OF_CHILDREN")
    private String noOfChildren;

    @Column(name = "NO_SCHOOL_GOING_CHILD")
    private String noSchoolGoingChild;

    @Column(name = "MARITAL_STATUS")
    private String maritalStatus;

    @Column(name = "NATIONALITY")
    private String nationality;

    @Column(name = "REGISTERED_DISABLED_FLAG")
    private String registeredDisabledFlag;

    @Column(name = "FATHER_NAME")
    private String fatherName;

    @Column(name = "MOBILE_NO")
    private String mobileNo;

    @Column(name = "PASSPORT_NO")
    private String passportNo;

    @Column(name = "PASSPORT_FROM_DATE")
    private String passportFromDate;

    @Column(name = "PASSPORT_TO_DATE")
    private String passportToDate;

    @Column(name = "ADHAAR_NO")
    private String adhaarNo;

    @Column(name = "NATURE_OF_DEPT")
    private String natureOfDept;

    @Column(name = "SOURCE_OF_APP")
    private String sourceOfApp;

    @Column(name = "BLOOD_TYPE")
    private String bloodType;

    @Column(name = "COUNTRY_OF_BIRTH")
    private String countryOfBirth;

    @Column(name = "COMMUNITY")
    private String community;

    @Column(name = "ADDRESS_ID")
    private Long addressId;

    @Column(name = "ADD1")
    private String add1;

    @Column(name = "ADD2")
    private String add2;

    @Column(name = "ADD3")
    private String add3;

    @Column(name = "TALUK")
    private String taluk;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "NATIONAL_IDENTIFIER")
    private String nationalIdentifier;

    @Column(name = "HIGH_QUALIFICATION")
    private String highQualification;

    @Column(name = "HIGH_QUALI_EST")
    private String highQualiEst;

    @Column(name = "QUALIFICATION")
    private String qualification;

    @Column(name = "PREVIOUS_EMP")
    private String previousEmp;

    @Column(name = "PREVIOUS_EXP")
    private String previousExp;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "JOB_ID", nullable = false)
    private Long jobId;

    @Column(name = "JOB")
    private String job;

    @Column(name = "GRADE_ID")
    private Long gradeId;

    @Column(name = "GRADE")
    private String grade;

    @Column(name = "PFNO")
    private String pfno;

    @Column(name = "UAN")
    private String uan;

    @Column(name = "ESINO")
    private String esino;

    @Column(name = "PAN_NO")
    private String panNo;

    @Column(name = "LOC", nullable = false)
    private String loc;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "REGION")
    private String region;

    @Column(name = "ORG_ID")
    private BigDecimal orgId;

    @Column(name = "DOJ")
    private LocalDateTime doj;

    @Column(name = "POSITION_ID")
    private Long positionId;

    @Column(name = "POSITION_NAME")
    private String positionName;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "ORG")
    private String org;

    @Column(name = "DIVISION")
    private String division;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "OFFER_DATE")
    private String offerDate;

    @Column(name = "PHONE_NO")
    private String phoneNo;

    @Column(name = "PHONE_LAST_UPDATE_DATE")
    private LocalDateTime phoneLastUpdateDate;

    @Column(name = "USER_STATUS")
    private String userStatus;

    @Column(name = "DRIVER_LIC_EXP_DATE")
    private String driverLicExpDate;

    @Column(name = "DRIVER_LICENSE_NO")
    private String driverLicenseNo;

    @Column(name = "SUPERVISOR_ID")
    private Long supervisorId;

    @Column(name = "SUPERVISOR_EMAIL")
    private String supervisorEmail;

    @Column(name = "SUPERVISOR_CODE")
    private String supervisorCode;

    @Column(name = "SUPERVISOR_NAME")
    private String supervisorName;

    @Column(name = "SUPERVISOR_GRADE")
    private String supervisorGrade;

    @Column(name = "BREEDER_BRANCH")
    private String breederBranch;

    @Column(name = "VENDOR_CODE")
    private String vendorCode;

    @Column(name = "VENDOR_NAME")
    private String vendorName;

    @Column(name = "TWO_WHEELER_LIC_NO")
    private String twoWheelerLicNo;

    @Column(name = "TWO_WHEELER_LIC_EXP")
    private String twoWheelerLicExp;

    @Column(name = "FOUR_WHEELER_LIC_NO")
    private String fourWheelerLicNo;

    @Column(name = "FOUR_WHEELER_LIC_EXP")
    private String fourWheelerLicExp;

    @Column(name = "RC_BOOK_NAME")
    private String rcBookName;

    @Column(name = "VEHICLE_INS_EXP")
    private String vehicleInsExp;

    @Column(name = "FUNCTION")
    private String function;

    @Column(name = "FUNCTION_SUPERVISOR_CODE")
    private String functionSupervisorCode;

    @Column(name = "FUNCTION_SUPERVISOR")
    private String functionSupervisor;

    @Column(name = "COSTING_BU")
    private String costingBu;

    @Column(name = "COSTING_DIVISION")
    private String costingDivision;

    @Column(name = "COSTING_LOCATION")
    private String costingLocation;

    @Column(name = "COST_DIV_DESC")
    private String costDivDesc;

    @Column(name = "COST_LOC_DESC")
    private String costLocDesc;

    @Column(name = "INVENTORY_ORGANIZATION_ID")
    private Long inventoryOrganizationId;

    @Column(name = "CONTACT_NAME")
    private String contactName;

    @Column(name = "CONTACT_TYPE")
    private String contactType;

    @Column(name = "CONTACT_PHONE")
    private String contactPhone;

    @Column(name = "PERZ_MAIL")
    private String perzMail;

    @Column(name = "BANK_ACC_NO")
    private String bankAccNo;

    @Column(name = "BANK_NAME")
    private String bankName;

    @Column(name = "EFFECTIVE_START_DATE", nullable = false)
    private LocalDateTime effectiveStartDate;

    @Column(name = "EFFECTIVE_END_DATE", nullable = false)
    private LocalDateTime effectiveEndDate;

    @Column(name = "GUEST_HOUSE")
    private String guestHouse;

    @Column(name = "HR_LOCATION")
    private String hrLocation;

    @Column(name = "LEDGER_ID", nullable = false)
    private Long ledgerId;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "GL_REGION")
    private String glRegion;

    @Column(name = "GL_BRANCH", nullable = false)
    private Long glBranch;

    @Column(name = "GL_DIVISION", nullable = false)
    private String glDivision;

    @Column(name = "GL_LOCATION")
    private String glLocation;

    @Column(name = "LOCATION_ID")
    private Long locationId;

    @Column(name = "DEPARTMENT")
    private String department;

    @Column(name = "REQ_SOURCE")
    private String reqSource;

    @Column(name = "REGION_ID")
    private BigDecimal regionId;

    @Column(name = "ORGANIZATION_ID", nullable = false)
    private Long organizationId;

    @Column(name = "BG_CODE")
    private String bgCode;

    @Column(name = "BG_NAME", nullable = false)
    private String bgName;

    @Column(name = "REGION_SHORT_CODE")
    private String regionShortCode;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "LAST_UPDATE_DATE")
    private LocalDateTime lastUpdateDate;

    @Column(name = "LAST_UPDATED_BY")
    private Long lastUpdatedBy;

    @Column(name = "LAST_UPDATE_LOGIN")
    private Long lastUpdateLogin;

    @Column(name = "PERIOD_OF_SERVICE_ID")
    private Long periodOfServiceId;

    @Column(name = "PERSON_LAST_UPDATE_DATE")
    private LocalDateTime personLastUpdateDate;

    @Column(name = "MOBILE_LAST_UPDATE_DATE")
    private LocalDateTime mobileLastUpdateDate;

    @Column(name = "QUALI_QUALIFICATION")
    private String qualiQualification;

    @Column(name = "QUALI_ESTABLISHMENT")
    private String qualiEstablishment;

    @Column(name = "QUALI_GRADE_MARKK")
    private String qualiGradeMarkk;

    @Column(name = "QUALI_MODE_OF_STUDY")
    private String qualiModeOfStudy;

    @Column(name = "QUALI_SPECIALIZATION")
    private String qualiSpecialization;

    @Column(name = "QUALI_START_DATE")
    private LocalDateTime qualiStartDate;

    @Column(name = "QUALI_END_DATE")
    private LocalDateTime qualiEndDate;

    @Column(name = "INV_ORGN_CODE")
    private String invOrgnCode;

    @Column(name = "JOBLOCATION_PG")
    private String joblocationPg;

    @Column(name = "EMPLOYEE_CODE_NEW_PG")
    private String employeeCodeNewPg;

    @Column(name = "STATE_PG")
    private String statePg;

    @Column(name = "WORKSITE_PG")
    private String worksitePg;

    @Column(name = "STRATEGIC_BUSINESS_UNIT_PG")
    private String strategicBusinessUnitPg;

    @Column(name = "ZONE_PG")
    private String zonePg;

    @Column(name = "BUSINESS_UNIT_PG")
    private String businessUnitPg;

    @Column(name = "CLUSTER_NAME_PG")
    private String clusterNamePg;

    @Column(name = "DIVISION_PG")
    private String divisionPg;

    @Column(name = "DEPARTMENT_PG")
    private String departmentPg;

    @Column(name = "FUNCTIONAL_GROUPING_PG")
    private String functionalGroupingPg;

    @Column(name = "SUB_LEVEL_PG")
    private String subLevelPg;

    @Column(name = "SUB_DEPARTMENT_PG")
    private String subDepartmentPg;

    @Column(name = "SUBDIVISION_PG")
    private String subdivisionPg;

    @Column(name = "POSITION_CODE_PG")
    private String positionCodePg;

    @Column(name = "POS_CODE")
    private String posCode;

    @Column(name = "BANK_BRANCH")
    private String bankBranch;

    @Column(name = "BANK_IFSC")
    private String bankIfsc;

    @Column(name = "USER_NAME")
    private String userName;

}