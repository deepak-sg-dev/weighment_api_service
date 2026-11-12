package com.suguna.weighment_api_service.dto.weighment.schedule;

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
public class ScheduleBranchDto {

    private String id;
    private String code;
    private String name;
    /** Display label derived from {@link #name} (e.g. {@code INDAPUR} from {@code M47:INDAPUR BROILER BRANCH-MH}). */
    private String shortName;
    /** HR location address for DC print ({@code hr_locations_all} via {@code sug_organization_mv}). */
    private String address;
    private String city;
    private String postalCode;
    private String state;
    /** Branch GSTIN from {@code sug_gst_pkg.get_reg_no('GSTN','O', inventory_organization_id)}. */
    private String gstNo;
}
