package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Indent-level customer on {@code sug_mai_birds_lifting_order} ({@code CUSTOMERCODE}, {@code CUS_NAME},
 * {@code CUS_LOCATION}). For retail this is the combined lifting customer, not a per-shop DTL line.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCustomerDto {

    private String code;
    private String name;
    private String location;
}
