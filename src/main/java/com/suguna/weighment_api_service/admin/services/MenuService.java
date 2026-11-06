package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.MenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuTreeDTO;
import com.suguna.weighment_api_service.admin.entities.PosMenu;

import java.util.List;

public interface MenuService {



    MenuResponseDTO createMenu(MenuRequestDTO dto);

    MenuResponseDTO updateMenu(Long id, MenuRequestDTO dto);

    MenuResponseDTO getMenuById(Long id);

    List<MenuResponseDTO> getAllMenus();

    List<MenuTreeDTO> getMenuTree(String application);

    List<MenuResponseDTO> getMenusByRole(Long roleId);



    void deleteMenu(Long id);

    void restoreMenu(Long id);


}
