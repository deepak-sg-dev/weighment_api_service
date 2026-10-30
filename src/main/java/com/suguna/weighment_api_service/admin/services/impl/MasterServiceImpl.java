package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDepotDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.VehicleDropdownDTO;
import com.suguna.weighment_api_service.admin.entities.Farmer;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.entities.Supervisor;
import com.suguna.weighment_api_service.admin.entities.Trader;
import com.suguna.weighment_api_service.admin.entities.TraderDepot;
import com.suguna.weighment_api_service.admin.entities.Vehicle;
import com.suguna.weighment_api_service.admin.repositories.FarmerRepository;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.repositories.SupervisorRepository;
import com.suguna.weighment_api_service.admin.repositories.TraderDepotRepository;
import com.suguna.weighment_api_service.admin.repositories.TraderRepository;
import com.suguna.weighment_api_service.admin.repositories.VehicleRepository;
import com.suguna.weighment_api_service.admin.services.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MasterServiceImpl implements MasterService {

    @Autowired
    private OrganizationBranchRepository organizationBranchRepository;

    @Autowired
    private TraderRepository traderRepository;

    @Autowired
    private TraderDepotRepository traderDepotRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Override
    public List<BranchDropdownDTO> getBranches() {

        return organizationBranchRepository
                .findByDivisionNameIgnoreCase("Broiler")
                .stream()
                .map(this::mapBranch)
                .toList();
    }

    @Override
    public List<TraderDropdownDTO> getTraders(
            Long branchId) {

        return traderRepository
                .findAll()
                .stream()
                .map(this::mapTrader)
                .toList();
    }

    @Override
    public List<TraderDepotDropdownDTO> getTraderDepots(
            Long traderId) {

        Map<String, TraderDepotDropdownDTO> uniqueDepots =
                new LinkedHashMap<>();

        traderDepotRepository
                .findByCustomerId(traderId)
                .forEach(depot -> {

                    String location =
                            depot.getShipLocation();

                    if (location != null &&
                            !uniqueDepots.containsKey(location)) {

                        uniqueDepots.put(
                                location,
                                mapTraderDepot(depot));
                    }
                });

        return uniqueDepots.values()
                .stream()
                .toList();
    }

    @Override
    public List<VehicleDropdownDTO> getVehicles(
            Long branchId) {

        return vehicleRepository
                .findByOperatingUnit(
                        BigDecimal.valueOf(branchId))
                .stream()
                .map(this::mapVehicle)
                .toList();
    }

    @Override
    public List<FarmerDropdownDTO> getFarmers(
            Long branchId) {

        OrganizationBranch branch =
                organizationBranchRepository
                        .findById(branchId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Branch not found"));

        String branchCode =
                branch.getBranchCode();

        return farmerRepository
                .findByBranchCode(branchCode)
                .stream()
                .map(this::mapFarmer)
                .toList();
    }

    @Override
    public List<SupervisorDropdownDTO> getSupervisors(
            Long branchId) {

        Map<String, SupervisorDropdownDTO> uniqueSupervisors =
                new LinkedHashMap<>();

        supervisorRepository
                .findByOrganizationIdAndApplication(
                        branchId,
                        "BRO-WEIGHMENT")
                .forEach(supervisor -> {

                    String empCode =
                            supervisor.getEmpCode();

                    if (empCode != null &&
                            !uniqueSupervisors.containsKey(empCode)) {

                        uniqueSupervisors.put(
                                empCode,
                                mapSupervisor(supervisor));
                    }
                });

        return uniqueSupervisors.values()
                .stream()
                .toList();
    }

    private BranchDropdownDTO mapBranch(
            OrganizationBranch branch) {

        BranchDropdownDTO dto =
                new BranchDropdownDTO();

        if (branch.getBranchId() != null) {
            dto.setId(
                    branch.getBranchId().longValue());
        }

        dto.setCode(
                branch.getBranchCode());

        dto.setName(
                branch.getBranchName());

        return dto;
    }

    private TraderDropdownDTO mapTrader(
            Trader trader) {

        TraderDropdownDTO dto =
                new TraderDropdownDTO();

        dto.setId(
                trader.getCustomerId());

        dto.setName(
                trader.getCustomerName());

        return dto;
    }

    private TraderDepotDropdownDTO mapTraderDepot(
            TraderDepot depot) {

        TraderDepotDropdownDTO dto =
                new TraderDepotDropdownDTO();

        dto.setId(
                depot.getCustomerId());

        dto.setName(
                depot.getShipLocation());

        return dto;
    }

    private VehicleDropdownDTO mapVehicle(
            Vehicle vehicle) {

        VehicleDropdownDTO dto =
                new VehicleDropdownDTO();

        dto.setId(
                vehicle.getVehicleNo());

        dto.setName(
                vehicle.getVehicleNo());

        return dto;
    }

    private FarmerDropdownDTO mapFarmer(
            Farmer farmer) {

        FarmerDropdownDTO dto =
                new FarmerDropdownDTO();

        dto.setId(
                farmer.getFarmerCode());

        dto.setName(
                farmer.getFarmerName());

        return dto;
    }

    private SupervisorDropdownDTO mapSupervisor(
            Supervisor supervisor) {

        SupervisorDropdownDTO dto =
                new SupervisorDropdownDTO();

        dto.setId(
                supervisor.getEmpCode());

        dto.setName(
                supervisor.getEmpName());

        return dto;
    }
}