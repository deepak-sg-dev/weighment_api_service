package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeighmentDcLineDto {

    private Integer sequence;
    private Integer birdsApplied;
    private Double net;
    private Double averageBirdWeight;
    private Double weightDifference;
    private String category;
}
