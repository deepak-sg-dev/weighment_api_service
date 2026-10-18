package com.suguna.weighment_api_service.admin.mappers;

import com.suguna.weighment_api_service.admin.dtos.requests.MenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuResponseDTO;
import com.suguna.weighment_api_service.admin.entities.PosMenu;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {


    public PosMenu toEntity(MenuRequestDTO dto) {
        PosMenu menu = new PosMenu();
        menu.setApplication(dto.getApplication());
        menu.setMenuName(dto.getMenuName());
        menu.setMenuPath(dto.getMenuPath());
        menu.setParentId(dto.getParentId());
        menu.setMenuFor(dto.getMenuFor());
        menu.setMenuType(dto.getMenuType());
        return menu;
    }


    public MenuResponseDTO toDTO(PosMenu menu) {
        MenuResponseDTO dto = new MenuResponseDTO();
        dto.setId(menu.getId());
        dto.setApplication(menu.getApplication());
        dto.setMenuName(menu.getMenuName());
        dto.setMenuPath(menu.getMenuPath());
        dto.setParentId(menu.getParentId());
        dto.setMenuFor(menu.getMenuFor());
        dto.setStatus(menu.getStatus());
        dto.setMenuType(menu.getMenuType());
        return dto;
    }



}
