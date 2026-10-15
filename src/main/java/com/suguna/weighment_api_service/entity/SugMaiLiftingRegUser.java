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
 * Maps {@code SUG.SUG_MAI_LIFTING_REG_USER}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugMaiLiftingRegUser.SugMaiLiftingRegUserId.class)
@Entity
@Table(name = "SUG_MAI_LIFTING_REG_USER", schema = "SUG")
public class SugMaiLiftingRegUser {

    @Id
    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "BRANCHID")
    private BigDecimal branchid;

    @Column(name = "BRANCHCODE")
    private String branchcode;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Id
    @Column(name = "DEVICE_ID")
    private String deviceId;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SugMaiLiftingRegUserId implements java.io.Serializable {
        private String userName;
        private String deviceId;
    }
}
