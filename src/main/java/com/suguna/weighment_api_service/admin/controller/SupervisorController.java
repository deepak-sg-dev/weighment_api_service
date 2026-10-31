package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;
import com.suguna.weighment_api_service.admin.services.SupervisorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<SupervisorResponseDTO>>
    getSupervisors(
            @RequestParam Long branchId) {

        return ResponseEntity.ok(
                supervisorService.getSupervisors(
                        branchId));
    }

    @GetMapping("/{supervisorId}")
    public ResponseEntity<SupervisorResponseDTO>
    getSupervisor(
            @PathVariable String supervisorId) {

        return ResponseEntity.ok(
                supervisorService.getSupervisor(
                        supervisorId));
    }
}