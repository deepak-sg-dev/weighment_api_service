package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SUG_MAI_ROLE_MENU_ACCESS")
public class RoleMenuAccess {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "MENU_ID")
    private Long menuId;

    @Column(name = "CAN_READ")
    private String canRead;

    @Column(name = "CAN_WRITE")
    private String canWrite;

    @Column(name = "CAN_UPDATE")
    private String canUpdate;

    @Column(name = "CAN_DELETE")
    private String canDelete;

    @Column(name = "STATUS")
    private String status;
}