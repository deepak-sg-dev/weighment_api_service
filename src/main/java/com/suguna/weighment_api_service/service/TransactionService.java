package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.repository.TransactionRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionSqlClient transactionSqlClient;
    private final ScheduleRepository scheduleRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionSqlClient transactionSqlClient,
            ScheduleRepository scheduleRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionSqlClient = transactionSqlClient;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public SubmitCompletedWeighmentResponse submitCompletedWeighment(
            AuthenticatedSupervisor supervisor, SubmitCompletedWeighmentRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        validateSupervisor(supervisor, request.getSupervisorId());
        validateSubmissionEvidence(request);

        String supervisorCode = supervisor.getSupervisor().getCode();
        String scheduleId = WeighmentSubmitKeys.normalizeScheduleId(request.getScheduleId());
        String transactionKey = WeighmentSubmitKeys.transactionKey(scheduleId);

        LiftingSchedule schedule = scheduleRepository
                .findByScheduleIdAndSupervisorId(scheduleId, supervisorCode)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "Schedule not found with id: "
                                + scheduleId
                                + " for supervisor "
                                + supervisorCode));

        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new ConflictException(ErrorCode.SCHEDULE_CLOSED, "Schedule is already completed");
        }

        var existing = transactionSqlClient.findByScheduleId(scheduleId, supervisorCode);
        if (existing.isPresent() && transactionSqlClient.hasWeighmentDetailLines(transactionKey)) {
            return existing.get().toResponse(scheduleId);
        }

        return transactionRepository.submitCompleted(supervisorCode, request);
    }

    public TransactionAcknowledgementResponse getAcknowledgement(
            AuthenticatedSupervisor supervisor, String transactionId) {
        return transactionRepository.getAcknowledgement(transactionId);
    }

    public TransactionActionResponse retryTransaction(
            AuthenticatedSupervisor supervisor,
            String transactionId,
            TransactionRetryRequest request) {
        return transactionRepository.retryTransaction(transactionId, request);
    }

    public TransactionActionResponse submitCorrection(
            AuthenticatedSupervisor supervisor,
            String transactionId,
            TransactionCorrectionRequest request) {
        return transactionRepository.submitCorrection(transactionId, request);
    }

    private void validateDevice(AuthenticatedSupervisor supervisor, String deviceId) {
        if (deviceId == null || !supervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }

    private void validateSupervisor(AuthenticatedSupervisor supervisor, String supervisorId) {
        if (supervisorId == null || supervisorId.isBlank()) {
            return;
        }
        String code = supervisor.getSupervisor().getCode();
        String id = supervisor.getSupervisor().getId();
        if (!supervisorId.trim().equals(code) && !supervisorId.trim().equals(id)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "supervisorId does not match authenticated session");
        }
    }

    private void validateSubmissionEvidence(SubmitCompletedWeighmentRequest request) {
        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        if (farm == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Farm validation evidence is required");
        }
        if (!Boolean.TRUE.equals(farm.getFarmerOtpVerified())) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Farmer OTP verification must be completed before submission");
        }
        if (!Boolean.TRUE.equals(farm.getGeofencePassed())) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Geofence validation failed");
        }
    }
}
