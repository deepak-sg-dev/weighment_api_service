package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateMenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;
import com.suguna.weighment_api_service.admin.services.MenuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menus")
@Tag(
        name = "Menu Management",
        description = "Menu management APIs"
)
public class MenuController {

    @Autowired
    private MenuService service;

    @GetMapping
    public MenuListResponseDTO getMenus(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String menuType) {

        return service.getMenus(
                search,
                status,
                menuType);
    }

    @GetMapping("/parents")
    public MenuListResponseDTO getParentMenus() {

        return service.getParentMenus();
    }

    @PostMapping
    public UpdateResponseDTO createMenu(
            @RequestBody CreateMenuRequestDTO request) {

        return service.createMenu(request);
    }

    @PatchMapping("/{menuId}")
    public UpdateResponseDTO updateMenu(
            @PathVariable Long menuId,
            @RequestBody UpdateMenuRequestDTO request) {

        return service.updateMenu(
                menuId,
                request);
    }
}