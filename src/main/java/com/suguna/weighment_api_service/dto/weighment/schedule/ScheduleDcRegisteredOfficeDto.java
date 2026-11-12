package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Parsed {@code sug_fnd_pkg.get_reg_off_address(ledger_id)} for DC print. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDcRegisteredOfficeDto {

    private String name;
    private String address;
    private String phone;
    private String fax;
    private String cinNo;
    private String pan;
    private String email;
    private String website;
    private String customerCareNo;
    /** Full ERP string when the client wants one print line. */
    private String text;
}
