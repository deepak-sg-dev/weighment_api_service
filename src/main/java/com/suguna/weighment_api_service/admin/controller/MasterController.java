package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDepotDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.VehicleDropdownDTO;
import com.suguna.weighment_api_service.admin.services.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/masters")
public class MasterController {

    @Autowired
    private MasterService masterService;

    @GetMapping("/branches")
    public ResponseEntity<List<BranchDropdownDTO>>
    getBranches() {

        return ResponseEntity.ok(
                masterService.getBranches());
    }

    @GetMapping("/traders")
    public ResponseEntity<List<TraderDropdownDTO>>
    getTraders(
            @RequestParam Long branchId) {

        return ResponseEntity.ok(
                masterService.getTraders(branchId));
    }

    @GetMapping("/trader-depots")
    public ResponseEntity<List<TraderDepotDropdownDTO>>
    getTraderDepots(
            @RequestParam Long traderId) {

        return ResponseEntity.ok(
                masterService.getTraderDepots(traderId));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleDropdownDTO>>
    getVehicles(
            @RequestParam Long branchId) {

        return ResponseEntity.ok(
                masterService.getVehicles(branchId));
    }

    @GetMapping("/farmers")
    public ResponseEntity<List<FarmerDropdownDTO>>
    getFarmers(
            @RequestParam Long branchId) {

        return ResponseEntity.ok(
                masterService.getFarmers(branchId));
    }

    @GetMapping("/supervisors")
    public ResponseEntity<List<SupervisorDropdownDTO>>
    getSupervisors(
            @RequestParam Long branchId) {

        return ResponseEntity.ok(
                masterService.getSupervisors(branchId));
    }
}