// DeviceDataEntity_1.java
package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "SUG_MAI_DEVICEDATA")
@Getter
@NoArgsConstructor
public class DeviceDataEntity_1 {

    @Id
    @Column(name = "EMP_CODE")
    private String empCode;

    @Column(name = "EMP_NAME")
    private String empName;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;
}
