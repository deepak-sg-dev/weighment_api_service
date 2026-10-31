package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.BranchListResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.BranchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/branches")
@Tag(
        name = "Admin Authentication",
        description = "Admin authentication and user management APIs"
)
public class BranchController {

    @Autowired
    private BranchService branchService;

    @GetMapping
    public ResponseEntity<ApiResponse<BranchListResponseDTO>> getBranches(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long branchId) {

        return Response.success(
                "Branches fetched successfully",
                branchService.getBranches(
                        page,
                        limit,
                        search,
                        branchId)
        );
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchDetailResponseDTO>>
    getBranchById(
            @PathVariable Long branchId) {

        return Response.success(
                "Branch details fetched successfully",
                branchService.getBranchById(branchId)
        );
    }
}