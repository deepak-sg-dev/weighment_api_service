package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.auth.DriverVehicleMappingRow;
import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.integration.oracle.DriverVehicleMappingSqlClient;
import com.suguna.weighment_api_service.repository.DeviceRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceSessionValidatorTest {

    @Test
    void acceptsSameIdIgnoringCaseAndPunctuation() {
        DeviceSessionValidator validator = validator(mock(DeviceRegistry.class), mock(DriverVehicleMappingSqlClient.class));
        AuthenticatedSupervisor session = employee("AbC-123");
        assertDoesNotThrow(() -> validator.requireMatches(session, "abc123"));
    }

    @Test
    void acceptsLoginAliasThatResolvesToSessionAndroidId() {
        DeviceRegistry registry = mock(DeviceRegistry.class);
        when(registry.findByDeviceId("98765"))
                .thenReturn(Optional.of(DeviceRegistration.builder()
                        .deviceId("ANDROID-1")
                        .enabled(true)
                        .authorized(true)
                        .supervisor(SupervisorProfile.builder().id("1").code("039192").active(true).build())
                        .build()));
        DeviceSessionValidator validator = validator(registry, mock(DriverVehicleMappingSqlClient.class));
        assertDoesNotThrow(() -> validator.requireMatches(employee("ANDROID-1"), "98765"));
    }

    @Test
    void driverAcceptsBodyDeviceIdWhenJwtIsOnVehicleMaster() {
        DriverVehicleMappingSqlClient mapping = mock(DriverVehicleMappingSqlClient.class);
        when(mapping.findByCompactVehicleNo("TN34R4567"))
                .thenReturn(List.of(new DriverVehicleMappingRow(
                        "1",
                        "TN34R4567",
                        "P",
                        "ZADDKKZTH6ETV6H",
                        null,
                        null,
                        "DRIVER",
                        null,
                        null,
                        null)));
        DeviceSessionValidator validator = validator(mock(DeviceRegistry.class), mapping);
        AuthenticatedSupervisor driver = new AuthenticatedSupervisor(
                "ZADDKKZTH6ETV6H",
                SupervisorProfile.builder().id("1").code("TN34R4567").active(true).build(),
                "DRIVER",
                "TN34R4567");
        assertDoesNotThrow(() -> validator.requireMatches(driver, "BT-001"));
    }

    @Test
    void rejectsUnknownDeviceIdForEmployee() {
        DeviceRegistry registry = mock(DeviceRegistry.class);
        when(registry.findByDeviceId("OTHER")).thenReturn(Optional.empty());
        DeviceSessionValidator validator = validator(registry, mock(DriverVehicleMappingSqlClient.class));
        assertThrows(ApiException.class, () -> validator.requireMatches(employee("ANDROID-1"), "OTHER"));
    }

    private static DeviceSessionValidator validator(
            DeviceRegistry registry, DriverVehicleMappingSqlClient mapping) {
        return new DeviceSessionValidator(registry, mapping);
    }

    private static AuthenticatedSupervisor employee(String deviceId) {
        return new AuthenticatedSupervisor(
                deviceId, SupervisorProfile.builder().id("1").code("039192").active(true).build());
    }
}
