package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDepotDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.TraderDropdownDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.VehicleDropdownDTO;

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
}