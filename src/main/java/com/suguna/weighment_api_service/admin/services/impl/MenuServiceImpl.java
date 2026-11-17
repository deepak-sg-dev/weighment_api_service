package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Menu;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.services.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    private static final String APP_NAME = "WMT_APP";

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public MenuListResponseDTO getMenus(
            String search,
            String status,
            String menuType) {

        List<MenuItemDTO> items = menuRepository
                .findByApplicationOrderBySeqAsc(APP_NAME)
                .stream()
                .filter(m -> search == null
                        || search.isBlank()
                        || m.getMenuName().toLowerCase().contains(search.toLowerCase())
                        || m.getMenuPath().toLowerCase().contains(search.toLowerCase()))
                .filter(m -> status == null
                        || status.isBlank()
                        || "ALL".equalsIgnoreCase(status)
                        || status.equalsIgnoreCase(m.getStatus()))
                .filter(m -> menuType == null
                        || menuType.isBlank()
                        || "ALL".equalsIgnoreCase(menuType)
                        || menuType.equalsIgnoreCase(m.getMenuType()))
                .map(m -> {
                    MenuItemDTO dto = new MenuItemDTO();
                    dto.setId(m.getId());
                    dto.setName(m.getMenuName());
                    dto.setPath(m.getMenuPath());
                    return dto;
                })
                .toList();

        MenuListResponseDTO response = new MenuListResponseDTO();
        response.setItems(items);

        return response;
    }

    @Override
    public MenuListResponseDTO getParentMenus() {

        List<MenuItemDTO> items = menuRepository
                .findByApplicationAndMenuTypeAndStatusOrderBySeqAsc(
                        APP_NAME,
                        "MAIN",
                        "A")
                .stream()
                .map(m -> {
                    MenuItemDTO dto = new MenuItemDTO();
                    dto.setId(m.getId());
                    dto.setName(m.getMenuName());
                    dto.setPath(m.getMenuPath());
                    return dto;
                })
                .toList();

        MenuListResponseDTO response = new MenuListResponseDTO();
        response.setItems(items);

        return response;
    }

    @Override
    public UpdateResponseDTO createMenu(
            CreateMenuRequestDTO request) {

        Menu menu = new Menu();

        menu.setMenuName(request.getMenuName());
        menu.setMenuPath(request.getMenuPath());
        menu.setApplication(APP_NAME);
        menu.setParentId(request.getParentId());
        menu.setMenuType(request.getMenuType());
        menu.setMenuFor("EMPLOYEE");
        menu.setSeq(request.getSeq());
        menu.setStatus(request.getStatus());

        menuRepository.save(menu);

        UpdateResponseDTO response = new UpdateResponseDTO();
        response.setUpdated(true);
        return response;
    }

    @Override
    public UpdateResponseDTO updateMenu(
            Long menuId,
            UpdateMenuRequestDTO request) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        if (request.getMenuName() != null) {
            menu.setMenuName(request.getMenuName());
        }

        if (request.getMenuPath() != null) {
            menu.setMenuPath(request.getMenuPath());
        }

        if (request.getParentId() != null) {
            menu.setParentId(request.getParentId());
        }

        if (request.getMenuType() != null) {
            menu.setMenuType(request.getMenuType());
        }

        if (request.getSeq() != null) {
            menu.setSeq(request.getSeq());
        }

        if (request.getStatus() != null) {
            menu.setStatus(request.getStatus());
        }

        menuRepository.save(menu);

        UpdateResponseDTO response = new UpdateResponseDTO();
        response.setUpdated(true);
        return response;
    }
}