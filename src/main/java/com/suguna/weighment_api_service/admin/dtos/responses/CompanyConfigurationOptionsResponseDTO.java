package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompanyConfigurationOptionsResponseDTO {

    private List<String> operatingStyles;

    private List<String> defaultUnits;

    private List<String> dcPrintingStyles;
}