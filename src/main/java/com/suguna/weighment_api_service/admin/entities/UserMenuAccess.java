package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SUG_MAI_USER_MENU_ACCESS")
public class UserMenuAccess {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID")
    private Long userId;

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