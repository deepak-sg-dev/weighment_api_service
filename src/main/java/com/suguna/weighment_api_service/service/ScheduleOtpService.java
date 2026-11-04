package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.ApiV2Properties;
import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.constants.WeighmentWorkflowStep;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.weighment.otp.PartyOtpStatusDto;
import com.suguna.weighment_api_service.dto.weighment.otp.PartyType;
import com.suguna.weighment_api_service.dto.weighment.otp.ResendPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.ResendPartyOtpResponse;
import com.suguna.weighment_api_service.dto.weighment.otp.ScheduleOtpStatusResponse;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpRequest;
import com.suguna.weighment_api_service.dto.weighment.otp.VerifyPartyOtpResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.integration.oracle.RetailRetailerOtpResolution;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient;
import com.suguna.weighment_api_service.integration.oracle.ScheduleOtpSqlClient.ScheduleOtpRow;
import com.suguna.weighment_api_service.repository.RetailDeliveryRepository;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ScheduleOtpService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final ScheduleRepository scheduleRepository;
    private final ScheduleOtpSqlClient scheduleOtpSqlClient;
    private final RetailDeliveryRepository retailDeliveryRepository;
    private final ScheduleWorkflowService scheduleWorkflowService;
    private final OtpResendTracker otpResendTracker;
    private final ApiV2Properties apiV2Properties;
    private final OracleErpProperties erpProperties;

    public ScheduleOtpService(
            ScheduleRepository scheduleRepository,
            ScheduleOtpSqlClient scheduleOtpSqlClient,
            RetailDeliveryRepository retailDeliveryRepository,
            ScheduleWorkflowService scheduleWorkflowService,
            OtpResendTracker otpResendTracker,
            ApiV2Properties apiV2Properties,
            OracleErpProperties erpProperties) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleOtpSqlClient = scheduleOtpSqlClient;
        this.retailDeliveryRepository = retailDeliveryRepository;
        this.scheduleWorkflowService = scheduleWorkflowService;
        this.otpResendTracker = otpResendTracker;
        this.apiV2Properties = apiV2Properties;
        this.erpProperties = erpProperties;
    }

    public ScheduleOtpStatusResponse getOtpStatus(AuthenticatedSupervisor supervisor, String scheduleId) {
        ensureScheduleAccessible(supervisor, scheduleId);
        ScheduleOtpRow row = requireOtpRow(supervisor, scheduleId);

        PartyOtpStatusDto farmer = PartyOtpStatusDto.builder()
                .required(row.isFarmerOtpRequired())
                .mobile(row.farmerMobile())
                .build();

        PartyOtpStatusDto trader = PartyOtpStatusDto.builder()
                .required(row.isTraderOtpRequired())
                .mobile(null)
                .build();

        return ScheduleOtpStatusResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .farmer(farmer)
                .trader(trader)
                .build();
    }

    public VerifyPartyOtpResponse verifyPartyOtp(
            AuthenticatedSupervisor supervisor, String scheduleId, VerifyPartyOtpRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        ensureScheduleAccessible(supervisor, scheduleId);
        PartyType partyType = request.getPartyType();

        if (partyType == PartyType.CUSTOMER) {
            return verifyCustomerOtp(supervisor, scheduleId, request);
        }

        ScheduleOtpRow row = requireOtpRow(supervisor, scheduleId);
        boolean verified =
                switch (partyType) {
                    case FARMER -> verifyFarmer(row, request.getOtp());
                    case TRADER -> verifyTrader(row, request.getOtp());
                    case CUSTOMER -> false;
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

    public ResendPartyOtpResponse resendPartyOtp(AuthenticatedSupervisor supervisor, ResendPartyOtpRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        String scheduleId = ScheduleIdSupport.normalize(request.getScheduleId());
        ensureScheduleAccessible(supervisor, scheduleId);
        PartyType partyType = request.getPartyType();
        if (partyType == PartyType.CUSTOMER) {
            return resendCustomerOtp(supervisor, scheduleId, request.getDeliveryId());
        }
        String newOtp = generateOtp();
        scheduleOtpSqlClient.replacePartyOtp(
                supervisor.getSupervisor().getCode(), scheduleId, partyType, newOtp);
        int count = otpResendTracker.recordResend(scheduleId, partyType, null);
        return buildResendResponse(scheduleId, partyType, count);
    }

    private ResendPartyOtpResponse resendCustomerOtp(
            AuthenticatedSupervisor supervisor, String scheduleId, String deliveryId) {
        if (deliveryId == null || deliveryId.isBlank()) {
            throw new BadRequestException("deliveryId is required for CUSTOMER OTP resend");
        }
        RetailScheduleContext context = retailDeliveryRepository.loadRetailSchedule(
                supervisor.getSupervisor().getCode(), scheduleId);
        if (retailDeliveryRepository.isRetailerOtpVerified(context, deliveryId.trim())) {
            throw new ConflictException(ErrorCode.OTP_ALREADY_VERIFIED, "OTP is already verified for this delivery");
        }
        String newOtp = generateOtp();
        retailDeliveryRepository.replaceRetailerOtp(context, deliveryId.trim(), newOtp);
        int count = otpResendTracker.recordResend(scheduleId, PartyType.CUSTOMER, deliveryId.trim());
        return buildResendResponse(scheduleId, PartyType.CUSTOMER, count);
    }

    private VerifyPartyOtpResponse verifyCustomerOtp(
            AuthenticatedSupervisor supervisor, String scheduleId, VerifyPartyOtpRequest request) {
        if (request.getDeliveryId() == null || request.getDeliveryId().isBlank()) {
            throw new BadRequestException("deliveryId is required for CUSTOMER OTP verify");
        }
        if (!scheduleWorkflowService.isDeliveryStepEnabled(scheduleId, WeighmentWorkflowStep.OTP_VALIDATION)) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "OTP validation is disabled for this schedule");
        }
        RetailScheduleContext context = retailDeliveryRepository.loadRetailSchedule(
                supervisor.getSupervisor().getCode(), scheduleId);
        String deliveryId = request.getDeliveryId().trim();
        if (retailDeliveryRepository.isRetailerOtpVerified(context, deliveryId)) {
            throw new ConflictException(ErrorCode.OTP_ALREADY_VERIFIED, "OTP is already verified for this delivery");
        }
        RetailRetailerOtpResolution resolution = retailDeliveryRepository.resolveRetailerOtp(context, deliveryId);
        RetailRetailerOtpValidation.validate(erpProperties, resolution, request.getOtp());
        retailDeliveryRepository.markRetailerOtpVerified(context, deliveryId);
        return VerifyPartyOtpResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .partyType(PartyType.CUSTOMER)
                .message("customer OTP verified")
                .build();
    }

    private ResendPartyOtpResponse buildResendResponse(String scheduleId, PartyType partyType, int resendCount) {
        Instant sent = Instant.now();
        return ResendPartyOtpResponse.builder()
                .success(true)
                .message("OTP sent")
                .scheduleId(scheduleId)
                .partyType(partyType)
                .sentAt(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(sent.atOffset(ZoneOffset.ofHoursMinutes(5, 30))))
                .expiresInSeconds(apiV2Properties.getOtpExpiresSeconds())
                .resendCount(resendCount)
                .maxResends(apiV2Properties.getOtpResendMax())
                .nextResendAfterSeconds(apiV2Properties.getOtpResendCooldownSeconds())
                .build();
    }

    private static String generateOtp() {
        return String.format(Locale.ROOT, "%05d", OTP_RANDOM.nextInt(100_000));
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
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        return scheduleOtpSqlClient
                .findOtpContext(supervisor.getSupervisor().getCode(), normalizedScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", normalizedScheduleId));
    }

    private void ensureScheduleAccessible(AuthenticatedSupervisor supervisor, String scheduleId) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        scheduleRepository
                .findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisor.getSupervisor().getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", normalizedScheduleId));
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
            case CUSTOMER ->
                    new BusinessValidationException(ErrorCodes.INVALID_RETAILER_OTP, "Invalid customer OTP");
        };
    }
}
