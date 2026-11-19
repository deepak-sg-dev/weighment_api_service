package com.suguna.weighment_api_service.dto.auth;

import com.suguna.weighment_api_service.auth.DriverVehicleMappingRow;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DriverDto {

    /** Display name for the driver app profile (not a person record in ERP). */
    public static final String PROFILE_DISPLAY_NAME = "Delivery person";

    private String id;
    private String name;
    private String vehicleNo;
    private String vehicleType;
    private String mobile;
    private String deviceId;
    private String userType;

    public static DriverDto from(DriverVehicleMappingRow row, String requestDeviceId) {
        if (row == null) {
            return null;
        }
        return DriverDto.builder()
                .id(blankToNull(row.transId()))
                .name(PROFILE_DISPLAY_NAME)
                .vehicleNo(blankToNull(row.vehicleNo()))
                .vehicleType(blankToNull(row.vehicleType()))
                .mobile(blankToNull(row.mobileNo()))
                .deviceId(blankToNull(requestDeviceId))
                .userType(blankToNull(row.userType()))
                .build();
    }

    public static DriverDto from(AuthenticatedSupervisor session) {
        if (session == null || !session.isDriver()) {
            return null;
        }
        SupervisorProfile profile = session.getSupervisor();
        return DriverDto.builder()
                .id(profile == null ? null : blankToNull(profile.getId()))
                .name(PROFILE_DISPLAY_NAME)
                .vehicleNo(blankToNull(session.getVehicleNo()))
                .vehicleType(profile == null ? null : blankToNull(profile.getName()))
                .mobile(profile == null ? null : blankToNull(profile.getMobile()))
                .deviceId(blankToNull(session.getDeviceId()))
                .userType(session.getUserType())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
