package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;

public interface MenuService {

    MenuListResponseDTO getMenus(
            String search,
            String status,
            String menuType);

    MenuListResponseDTO getParentMenus();

    UpdateResponseDTO createMenu(
            CreateMenuRequestDTO request);

    UpdateResponseDTO updateMenu(
            Long menuId,
            UpdateMenuRequestDTO request);
}