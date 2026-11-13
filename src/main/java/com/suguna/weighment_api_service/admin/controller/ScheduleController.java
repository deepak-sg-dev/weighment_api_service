package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.ScheduleListResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/schedules")
@Tag(
        name = "Schedule Management",
        description = "Schedule management APIs"
)
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<ScheduleListResponseDTO>>
    getSchedules(

            @RequestParam(defaultValue = "1")
            Integer page,

            @RequestParam(defaultValue = "20")
            Integer limit,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            String supervisorId,

            @RequestParam(required = false)
            String traderId,

            @RequestParam(required = false)
            String depot,

            @RequestParam(required = false)
            String vehicleId,

            @RequestParam(required = false)
            String pickupDate,

            @RequestParam(required = false)
            String status) {

        return Response.success(
                "Schedules fetched successfully",
                scheduleService.getSchedules(
                        page,
                        limit,
                        search,
                        branchId,
                        supervisorId,
                        traderId,
                        depot,
                        vehicleId,
                        pickupDate,
                        status));
    }
}
