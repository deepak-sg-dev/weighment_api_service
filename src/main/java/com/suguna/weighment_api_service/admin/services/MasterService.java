package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.*;

import java.util.List;

public interface MasterService {

    List<BranchDropdownDTO> getBranches();

    List<TraderDropdownDTO> getTraders(
            Long branchId);

    List<TraderDepotDropdownDTO> getTraderDepots(
            Long traderId);

    List<VehicleDropdownDTO> getVehicles(
            Long branchId);

    List<FarmerDropdownDTO> getFarmers(
            Long branchId);

    List<SupervisorDropdownDTO> getSupervisors(
            Long branchId);
    List<RegionDropdownDTO> getRegions();
}