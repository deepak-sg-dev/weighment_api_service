// BirdsLiftingOrderEntity_1.java
package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SUG_MAI_BIRDS_LIFTING_ORDER")
@Getter
@Setter
@NoArgsConstructor
public class BirdsLiftingOrderEntity_1 {

    @Id
    @Column(name = "ERPINDENTID")
    private String erpIndentId;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "DI_CREATION_DATE")
    private LocalDateTime diCreationDate;

    @Column(name = "MOBILE_ORDER_NO")
    private String mobileOrderNo;

    @Column(name = "FARMERNAME")
    private String farmerName;

    @Column(name = "LIFTING_STATUS")
    private String liftingStatus;

    @Column(name = "ORDERTYPE")
    private String orderType;

    @Column(name = "NO_OF_KGS")
    private BigDecimal noOfKgs;

    @Column(name = "BRANCHCODE")
    private String branchCode;

    @Column(name = "MOBILEUSEREMPID")
    private Long mobileUserEmpId;

    @Column(name = "CUSTOMERCODE")
    private Long customerCode;

    @Column(name = "CUS_NAME")
    private String cusName;

    @Column(name = "DOWNLOAD_FLAG")
    private String downloadFlag;

    @Column(name = "DOWNLOADED_DATE")
    private LocalDateTime downloadedDate;
}
