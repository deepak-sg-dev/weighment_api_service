package com.suguna.weighment_api_service.dto.weighment.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UpdateScheduleVehicleRequestTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void placeholderNumberDoesNotHideVehicleNo() throws Exception {
        UpdateScheduleVehicleRequest request = mapper.readValue(
                """
                {"deviceId":"DEV1","vehicle":{"number":"-","vehicleNo":"TN-09-AB-1234",
                "driverName":"Murugan","driverMobile":"9568845752"}}
                """,
                UpdateScheduleVehicleRequest.class);
        assertEquals("TN09AB1234", request.getVehicle().getNumber());
    }

    @Test
    void hyphenatedNumberIsCompacted() throws Exception {
        UpdateScheduleVehicleRequest request = mapper.readValue(
                """
                {"deviceId":"DEV1","vehicle":{"number":"tn 09 ab 1234",
                "driverName":"Murugan","driverMobile":"9568845752"}}
                """,
                UpdateScheduleVehicleRequest.class);
        assertEquals("TN09AB1234", request.getVehicle().getNumber());
    }
}
