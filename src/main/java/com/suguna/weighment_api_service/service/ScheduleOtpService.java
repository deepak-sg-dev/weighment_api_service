package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.dto.weighment.otp.PartyOtpStatusDto;
import com.suguna.weighment_api_service.dto.weighment.otp.PartyType;
import com.suguna.weighment_api_service.dto.weighment.otp.ScheduleOtpStatusResponse;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient.ScheduleOtpRow;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ScheduleOtpService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleOtpSqlClient scheduleOtpSqlClient;

    public ScheduleOtpService(ScheduleRepository scheduleRepository, ScheduleOtpSqlClient scheduleOtpSqlClient) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleOtpSqlClient = scheduleOtpSqlClient;
    }

    public ScheduleOtpStatusResponse getOtpStatus(AuthenticatedSupervisor supervisor, String scheduleId) {
        ensureScheduleAccessible(supervisor, scheduleId);
        ScheduleOtpRow row = requireOtpRow(supervisor, scheduleId);

        return ScheduleOtpStatusResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .farmer(PartyOtpStatusDto.builder()
                        .required(row.isFarmerOtpRequired())
                        .mobile(row.farmerMobile())
                        .build())
                .trader(PartyOtpStatusDto.builder()
                        .required(row.isTraderOtpRequired())
                        .mobile(null)
                        .build())
                .build();
    }

    public VerifyPartyOtpResponse verifyPartyOtp(
            AuthenticatedSupervisor supervisor, String scheduleId, VerifyPartyOtpRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        ensureScheduleAccessible(supervisor, scheduleId);
        ScheduleOtpRow row = requireOtpRow(supervisor, scheduleId);

        PartyType partyType = request.getPartyType();
        boolean verified =
                switch (partyType) {
                    case FARMER -> verifyFarmer(row, request.getOtp());
                    case TRADER -> verifyTrader(row, request.getOtp());
                };

        if (!verified) {
            throw otpInvalid(partyType);
        }

        return VerifyPartyOtpResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .partyType(partyType)
                .message(partyType.name().toLowerCase(Locale.ROOT) + " OTP verified")
                .build();
    }

    private boolean verifyFarmer(ScheduleOtpRow row, String otp) {
        if (!row.isFarmerOtpRequired()) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Farmer OTP is not configured for this schedule");
        }
        return row.farmerOtpMatches(otp);
    }

    private boolean verifyTrader(ScheduleOtpRow row, String otp) {
        if (!row.isTraderOtpRequired()) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Trader OTP is not configured for this schedule");
        }
        return row.traderOtpMatches(otp);
    }

    private ScheduleOtpRow requireOtpRow(AuthenticatedSupervisor supervisor, String scheduleId) {
        return scheduleOtpSqlClient
                .findOtpContext(supervisor.getSupervisor().getCode(), scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
    }

    private void ensureScheduleAccessible(AuthenticatedSupervisor supervisor, String scheduleId) {
        scheduleRepository
                .findByScheduleIdAndSupervisorId(scheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (!supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }

    private static BusinessValidationException otpInvalid(PartyType partyType) {
        return switch (partyType) {
            case FARMER ->
                    new BusinessValidationException(ErrorCodes.INVALID_FARMER_OTP, "Invalid farmer OTP");
            case TRADER ->
                    new BusinessValidationException(ErrorCodes.INVALID_TRADER_OTP, "Invalid trader OTP");
        };
    }
}
