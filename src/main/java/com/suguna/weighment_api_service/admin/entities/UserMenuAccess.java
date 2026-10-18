package com.suguna.weighment_api_service.admin.entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUG_MAI_USER_MENU_ACCESS", schema = "SUG")
@Getter
@Setter
public class UserMenuAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_menu_seq")
    @SequenceGenerator(name = "user_menu_seq", sequenceName = "SUG_MAI_USER_MENU_S", allocationSize = 1)
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

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "STATUS")
    private String status;
}
