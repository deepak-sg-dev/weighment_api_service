package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.SupervisorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/supervisors")
@Tag(
        name = "Supervisor Management",
        description = "Supervisor management APIs"
)
public class SupervisorController {

    @Autowired
    private SupervisorService supervisorService;

    @GetMapping
    public ResponseEntity<ApiResponse<SupervisorListResponseDTO>>
    getSupervisors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam Long branchId) {

        return Response.success(
                "Supervisors fetched successfully",
                supervisorService.getSupervisors(
                        page,
                        limit,
                        search,
                        branchId)
        );
    }

    @GetMapping("/{supervisorId}")
    public ResponseEntity<ApiResponse<SupervisorResponseDTO>>
    getSupervisor(
            @PathVariable String supervisorId) {

        return Response.success(
                "Supervisor details fetched successfully",
                supervisorService.getSupervisor(
                        supervisorId)
        );
    }
}