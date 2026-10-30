package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Immutable;

/**
 * Maps {@code APPS.SUG_FARM_LIFTING_GPS_V}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Immutable
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugFarmLiftingGpsV.SugFarmLiftingGpsVId.class)
@Entity
@Table(name = "SUG_FARM_LIFTING_GPS_V", schema = "APPS")
public class SugFarmLiftingGpsV {

    @Column(name = "REGION_CODE")
    private String regionCode;

    @Id
    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;

    @Id
    @Column(name = "FARM_CODE")
    private String farmCode;

    @Column(name = "FARM_NAME_VILLAGE", nullable = false)
    private String farmNameVillage;

    @Column(name = "LAST_DC_DATE")
    private LocalDateTime lastDcDate;

    @Column(name = "LIFTING_LATITUDE")
    private BigDecimal liftingLatitude;

    @Column(name = "LIFTING_LONGITUDE")
    private BigDecimal liftingLongitude;

    @Column(name = "FARM_MST_LATITUDE")
    private BigDecimal farmMstLatitude;

    @Column(name = "FARM_MST_LONGITUDE")
    private BigDecimal farmMstLongitude;

    @Column(name = "DISTANCE")
    private BigDecimal distance;

    @Column(name = "WM_SUPERVISOR_CODE")
    private String wmSupervisorCode;

    @Column(name = "WM_SUPERVISOR_NAME")
    private String wmSupervisorName;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class SugFarmLiftingGpsVId implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String farmCode;
        private String branchCode;
    }
}