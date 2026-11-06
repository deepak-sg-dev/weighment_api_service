package com.suguna.weighment_api_service.admin.controller;



import com.suguna.weighment_api_service.admin.dtos.requests.MenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuTreeDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/menus")
@Tag(name = "Menu API", description = "Manage application menus")
public class MenuController {


    @Autowired
    private MenuService service;

    @Operation(summary = "Create Menu")
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponseDTO>> createMenu(
            @RequestBody MenuRequestDTO dto) {

        MenuResponseDTO menuResponseDTO = service.createMenu(dto);

        return Response.success("Menu Created successfully", menuResponseDTO);

    }

    @Operation(summary = "Update Menu by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> updateMenu(
            @PathVariable Long id,
            @RequestBody MenuRequestDTO dto) {

        MenuResponseDTO menuResponseDTO = service.updateMenu(id, dto);

        return Response.success("Menu Updated successfully", menuResponseDTO);

    }

    @Operation(summary = "Get Menu by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponseDTO>> getMenuById(
            @PathVariable Long id) {

        MenuResponseDTO menuResponseDTO = service.getMenuById(id);

        return Response.success("Menu Fetched successfully", menuResponseDTO);

    }

    @Operation(summary = "Get All Menus")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponseDTO>>> getAllMenus() {

        List<MenuResponseDTO> menuResponseDTOS = service.getAllMenus();

        return Response.success("Menu Fetched successfully", menuResponseDTOS);
    }

    @Operation(summary = "Get Menu Tre")
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<MenuTreeDTO>>> getMenuTree(
            @RequestParam String application) {

        List<MenuTreeDTO> menuResponseDTOS = service.getMenuTree(application);

        return Response.success("Menu Fetched successfully", menuResponseDTOS);


    }

    @Operation(summary = "Get Menu by role id")
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<?> getMenusByRole(@PathVariable Long roleId) {

        return Response.success(
                "Menus fetched successfully",
                service.getMenusByRole(roleId)
        );
    }


    @Operation(summary = "Delete Menu (Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {

        service.deleteMenu(id);

        return Response.success(
                "Menu deleted successfully",
                null
        );
    }


    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restoreMenu(@PathVariable Long id) {

        service.restoreMenu(id);

        return Response.success("Menu restored successfully", null);
    }




}
