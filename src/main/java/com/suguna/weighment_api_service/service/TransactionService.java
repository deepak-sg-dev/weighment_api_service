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
import com.suguna.weighment_api_service.dto.transaction.WeighmentSubmissionDetailResponse;
import com.suguna.weighment_api_service.dto.transaction.WeighmentSubmissionHistoryResponse;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.integration.oracle.LiftingScheduleSqlClient;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient.SubmissionHeaderSnapshot;
import com.suguna.weighment_api_service.integration.oracle.WeighmentDetailSqlClient;
import com.suguna.weighment_api_service.integration.oracle.WeighmentDetailSqlClient.StoredWeighmentDetailLine;
import com.suguna.weighment_api_service.mapper.WeighmentSubmissionMapper;
import com.suguna.weighment_api_service.repository.ScheduleRepository;
import com.suguna.weighment_api_service.repository.TransactionRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionSqlClient transactionSqlClient;
    private final WeighmentDetailSqlClient weighmentDetailSqlClient;
    private final WeighmentSubmissionMapper submissionMapper;
    private final ScheduleRepository scheduleRepository;
    private final LiftingScheduleSqlClient liftingScheduleSqlClient;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionSqlClient transactionSqlClient,
            WeighmentDetailSqlClient weighmentDetailSqlClient,
            WeighmentSubmissionMapper submissionMapper,
            ScheduleRepository scheduleRepository,
            LiftingScheduleSqlClient liftingScheduleSqlClient) {
        this.transactionRepository = transactionRepository;
        this.transactionSqlClient = transactionSqlClient;
        this.weighmentDetailSqlClient = weighmentDetailSqlClient;
        this.submissionMapper = submissionMapper;
        this.scheduleRepository = scheduleRepository;
        this.liftingScheduleSqlClient = liftingScheduleSqlClient;
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
            syncLiftingOrderAfterWeighment(supervisorCode, scheduleId, request);
            return existing.get().toResponse(scheduleId);
        }

        return transactionRepository.submitCompleted(supervisorCode, request);
    }

    private void syncLiftingOrderAfterWeighment(
            String supervisorCode, String scheduleId, SubmitCompletedWeighmentRequest request) {
        String saleType = weighmentDetailSqlClient.resolveBusinessModule(
                scheduleId, request.effectiveSaleType(), request.effectiveBirdType());
        liftingScheduleSqlClient.markLiftingCompletedAfterWeighment(supervisorCode, scheduleId, saleType);
    }

    public TransactionAcknowledgementResponse getAcknowledgement(
            AuthenticatedSupervisor supervisor, String transactionId) {
        return transactionRepository.getAcknowledgement(transactionId);
    }

    public WeighmentSubmissionDetailResponse getSubmissionDetail(
            AuthenticatedSupervisor supervisor, String scheduleId) {
        SubmissionLoad load = loadSubmission(supervisor, scheduleId);
        return submissionMapper.toDetailResponse(load.scheduleId(), load.header(), load.lines());
    }

    public WeighmentSubmissionHistoryResponse getSubmissionHistory(
            AuthenticatedSupervisor supervisor, String scheduleId) {
        SubmissionLoad load = loadSubmission(supervisor, scheduleId);
        return submissionMapper.toHistoryResponse(load.scheduleId(), load.header(), load.lines());
    }

    private SubmissionLoad loadSubmission(AuthenticatedSupervisor supervisor, String scheduleId) {
        String supervisorCode = supervisor.getSupervisor().getCode();
        String normalizedScheduleId = WeighmentSubmitKeys.normalizeScheduleId(scheduleId);
        scheduleRepository
                .findByScheduleIdAndSupervisorId(normalizedScheduleId, supervisorCode)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", normalizedScheduleId));

        SubmissionHeaderSnapshot header = transactionSqlClient
                .findSubmissionHeader(normalizedScheduleId, supervisorCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Weighment submission", normalizedScheduleId));

        String transactionKey = WeighmentSubmitKeys.transactionKey(normalizedScheduleId);
        List<StoredWeighmentDetailLine> lines = weighmentDetailSqlClient.listLiftingCompleteLines(transactionKey);
        if (lines.isEmpty()) {
            throw new ResourceNotFoundException("Weighment submission lines", normalizedScheduleId);
        }
        return new SubmissionLoad(normalizedScheduleId, header, lines);
    }

    private record SubmissionLoad(
            String scheduleId, SubmissionHeaderSnapshot header, List<StoredWeighmentDetailLine> lines) {}

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
