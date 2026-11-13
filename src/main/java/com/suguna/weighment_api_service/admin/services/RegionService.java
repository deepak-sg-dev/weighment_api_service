package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.RegionDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RegionListResponseDTO;

public interface RegionService {

    RegionListResponseDTO getRegions(
            Integer page,
            Integer limit,
            String search,
            Long regionId);

    RegionDetailResponseDTO getRegionById(Long regionId);
}
