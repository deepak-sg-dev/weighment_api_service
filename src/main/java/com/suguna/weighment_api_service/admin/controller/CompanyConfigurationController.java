package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.CompanyConfigurationResponseDTO;
import com.suguna.weighment_api_service.admin.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/configuration")
@Tag(
        name = "Company Configuration",
        description = "Company configuration management APIs"
)
public class CompanyConfigurationController {

    @Autowired
    private ConfigurationService service;

    @GetMapping("/company")
    public CompanyConfigurationResponseDTO getCompanyConfiguration() {

        return service.getCompanyConfiguration();
    }
}