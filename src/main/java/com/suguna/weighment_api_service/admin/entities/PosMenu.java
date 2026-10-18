package com.suguna.weighment_api_service.admin.entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUG_MAI_MENU", schema = "SUG")
@Getter
@Setter
public class PosMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menu_seq")
    @SequenceGenerator(sequenceName = "SUG_MAI_MENU_S", allocationSize = 1, name = "menu_seq")
    @Column(name = "ID")
    private long id;
    @Column(name = "APPLICATION")
    private String application;
    @Column(name = "MENU_NAME")
    private String menuName;
    @Column(name = "MENU_PATH")
    private String menuPath;
    @Column(name = "PARENT_ID")
    private Long parentId;
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
    @Column(name = "MENU_FOR")
    private String menuFor;
    @Column(name = "MENU_TYPE")
    private String menuType;
    @Column(name = "SEQ")
    private Integer sequence;
    @Column(name = "IS_DELETED")
    private int isDeleted = 0;
    @Column(name = "DELETED_BY")
    private Long deletedBy;
    @Column(name = "DELETED_AT")
    private Date deletedAt;
}
