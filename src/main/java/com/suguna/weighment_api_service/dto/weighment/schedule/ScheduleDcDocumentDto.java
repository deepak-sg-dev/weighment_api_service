package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional DC (delivery challan) print fields for GET schedule details. Values are null until ERP/admin
 * supplies them; clients should only use a field when it is present.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ScheduleDcDocumentDto {

    private String slipNo;
    private String lotNo;
    /** From item master {@code sug_gst_pkg.get_item_info('HSN', ...)}. */
    private String hsnCode;
    /** From item master {@code sug_gst_pkg.get_item_info('TAXCODE', ...)}. */
    private String taxCode;
    /** Tax rate percent when ERP supplies a numeric rate (optional; taxCode is the GST template code). */
    private BigDecimal taxRatePercent;
    private String traderGstNo;
    private String placeOfSupply;
    /** Branch region code from {@code sug_organization_mv.region_code} (e.g. {@code AS}). */
    private String region;
    /** Legal entity from registered office (e.g. {@code SUGUNA FOODS PRIVATE LIMITED}). */
    private String company;
    /** Print line {@code region-company}, e.g. {@code AS-SUGUNA FOODS PRIVATE LIMITED}. */
    private String regionCompany;
    /** Breed / strain (often same as bird type / product). */
    private String breedStrain;
    private String cinNo;
    private String panNo;
    /** From {@code sug_fnd_pkg.get_reg_off_address(ledger_id)}. */
    private ScheduleDcRegisteredOfficeDto registeredOffice;
    /** Bank name / “BANK DETAILS” on the printed DC. */
    private String bankDetails;
    private String bankBranch;
    private String bankAccountNo;
    private String bankIfsc;

    public static ScheduleDcDocumentDto empty() {
        return ScheduleDcDocumentDto.builder().build();
    }
}
