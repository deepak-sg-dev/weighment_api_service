package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SUG_MAI_MENU")
public class Menu {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "menu_seq")
    @SequenceGenerator(
            name = "menu_seq",
            sequenceName = "SUG_MAI_MENU_S",
            allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MENU_NAME")
    private String menuName;

    @Column(name = "MENU_PATH")
    private String menuPath;

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "MENU_TYPE")
    private String menuType;

    @Column(name = "MENU_FOR")
    private String menuFor;

    @Column(name = "SEQ")
    private Long seq;

    @Column(name = "STATUS")
    private String status;
}