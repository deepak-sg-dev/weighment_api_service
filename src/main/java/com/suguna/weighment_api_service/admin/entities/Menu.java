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
@Table(name = "SUG_MAI_MENU")
public class Menu {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "MENU_NAME")
    private String menuName;

    @Column(name = "MENU_PATH")
    private String menuPath;

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "STATUS")
    private String status;
}