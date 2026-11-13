package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.RegionDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RegionListResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.RegionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/regions")
@Tag(
        name = "Admin Regions",
        description = "Region management APIs"
)
public class AdminRegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping
    public ResponseEntity<ApiResponse<RegionListResponseDTO>> getRegions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long regionId) {

        return Response.success(
                "Regions fetched successfully",
                regionService.getRegions(page, limit, search, regionId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegionDetailResponseDTO>> getRegionById(
            @PathVariable Long id) {

        return Response.success(
                "Region details fetched successfully",
                regionService.getRegionById(id)
        );
    }
}
