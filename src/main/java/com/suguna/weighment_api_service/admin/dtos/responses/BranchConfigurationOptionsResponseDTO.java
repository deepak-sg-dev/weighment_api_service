package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BranchConfigurationOptionsResponseDTO {

    private List<String> operatingStyles;

    private List<String> units;

    private List<String> userCategories;
}