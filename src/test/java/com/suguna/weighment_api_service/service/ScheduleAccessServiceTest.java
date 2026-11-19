package com.suguna.weighment_api_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleAccessServiceTest {

    private ScheduleRepository scheduleRepository;
    private RetailDeliveryRepository retailDeliveryRepository;
    private ScheduleAccessService accessService;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(ScheduleRepository.class);
        retailDeliveryRepository = mock(RetailDeliveryRepository.class);
        accessService = new ScheduleAccessService(scheduleRepository, retailDeliveryRepository);
    }

    @Test
    void driverUsesLiftingSupervisorEmpNotVehiclePlate() {
        AuthenticatedSupervisor driver = driverSession("TN34R4567");
        LiftingSchedule lifting = new LiftingSchedule();
        lifting.setScheduleId("4747482");
        lifting.setSupervisorId("10001234");
        when(scheduleRepository.findByScheduleIdAndVehicleNo("4747482", "TN34R4567"))
                .thenReturn(Optional.of(lifting));

        assertEquals("10001234", accessService.liftingSupervisorCode(driver, "4747482"));
        accessService.loadRetailSchedule(driver, "4747482");
        verify(retailDeliveryRepository).loadRetailSchedule("10001234", "4747482");
    }

    @Test
    void driverUnknownVehicleSchedule_notFound() {
        AuthenticatedSupervisor driver = driverSession("TN34R4567");
        when(scheduleRepository.findByScheduleIdAndVehicleNo("4747482", "TN34R4567"))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accessService.loadRetailSchedule(driver, "4747482"));
    }

    @Test
    void appSupervisorKeepsEmpCode() {
        SupervisorProfile profile = SupervisorProfile.builder()
                .id("emp")
                .code("10001234")
                .active(true)
                .build();
        AuthenticatedSupervisor app = new AuthenticatedSupervisor("DEV1", profile, "APP", null);
        assertEquals("10001234", accessService.liftingSupervisorCode(app, "4747482"));
        accessService.loadRetailSchedule(app, "4747482");
        verify(retailDeliveryRepository).loadRetailSchedule("10001234", "4747482");
    }

    private static AuthenticatedSupervisor driverSession(String plate) {
        SupervisorProfile profile = SupervisorProfile.builder()
                .id("1")
                .code(plate)
                .active(true)
                .build();
        return new AuthenticatedSupervisor("3456789234", profile, "DRIVER", plate);
    }
}
