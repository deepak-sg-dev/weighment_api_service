package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDcRegisteredOfficeDto;
import org.junit.jupiter.api.Test;

class RegisteredOfficeAddressSupportTest {

    @Test
    void parsesErpRegisteredOfficeBlob() {
        String raw =
                """
                Registered office:SUGUNA FOODS PRIVATE LIMITED,1057,Jaya Enclave,6th Floor,Avinashi Road,Coimbatore-641018.
                Ph No:+91 422 4073000,Fax No:+91 422 4073333, CIN No:U01222TZ1994PTC004951,PAN:AADCS0655F
                Email:corporate@sugunafoods.com,Website:www.sugunafoods.com, Customer Care No: 1800 103 43 43
                """;
        ScheduleDcRegisteredOfficeDto dto = RegisteredOfficeAddressSupport.parse(raw);
        assertNotNull(dto);
        assertEquals("SUGUNA FOODS PRIVATE LIMITED", dto.getName());
        assertEquals(
                "1057,Jaya Enclave,6th Floor,Avinashi Road,Coimbatore-641018.", dto.getAddress());
        assertEquals("+91 422 4073000", dto.getPhone());
        assertEquals("U01222TZ1994PTC004951", dto.getCinNo());
        assertEquals("AADCS0655F", dto.getPan());
        assertEquals("corporate@sugunafoods.com", dto.getEmail());
        assertEquals("www.sugunafoods.com", dto.getWebsite());
        assertEquals("1800 103 43 43", dto.getCustomerCareNo());
    }
}
