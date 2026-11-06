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
import com.suguna.weighment_api_service.config.ApiV2Properties;
import com.suguna.weighment_api_service.constants.WeighmentWorkflowStep;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.FeedStockInfo;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;
import com.suguna.weighment_api_service.util.LiveReadingValidation;
import com.suguna.weighment_api_service.util.WeighmentEntryCategory;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
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
    private final ScheduleWorkflowService scheduleWorkflowService;
    private final ScheduleRetailEnrichmentService retailEnrichmentService;
    private final ApiV2Properties apiV2Properties;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionSqlClient transactionSqlClient,
            WeighmentDetailSqlClient weighmentDetailSqlClient,
            WeighmentSubmissionMapper submissionMapper,
            ScheduleRepository scheduleRepository,
            LiftingScheduleSqlClient liftingScheduleSqlClient,
            ScheduleWorkflowService scheduleWorkflowService,
            ScheduleRetailEnrichmentService retailEnrichmentService,
            ApiV2Properties apiV2Properties) {
        this.transactionRepository = transactionRepository;
        this.transactionSqlClient = transactionSqlClient;
        this.weighmentDetailSqlClient = weighmentDetailSqlClient;
        this.submissionMapper = submissionMapper;
        this.scheduleRepository = scheduleRepository;
        this.liftingScheduleSqlClient = liftingScheduleSqlClient;
        this.scheduleWorkflowService = scheduleWorkflowService;
        this.retailEnrichmentService = retailEnrichmentService;
        this.apiV2Properties = apiV2Properties;
    }

    @Transactional
    public SubmitCompletedWeighmentResponse submitCompletedWeighment(
            AuthenticatedSupervisor supervisor, SubmitCompletedWeighmentRequest request) {
        validateDevice(supervisor, request.getDeviceId());
        validateSupervisor(supervisor, request.getSupervisorId());
        String scheduleId = WeighmentSubmitKeys.normalizeScheduleId(request.getScheduleId());
        String supervisorCode = supervisor.getSupervisor().getCode();

        LiftingSchedule schedule = scheduleRepository
                .findByScheduleIdAndSupervisorId(scheduleId, supervisorCode)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "Schedule not found with id: "
                                + scheduleId
                                + " for supervisor "
                                + supervisorCode));

        validateWeighmentEntries(scheduleId, request.getWeighmentEntries());
        String submissionSaleType = resolveSubmissionSaleType(schedule, request);
        validateSubmissionEvidence(submissionSaleType, scheduleId, request);
        validateRetailLiftingFarmMetrics(submissionSaleType, request);
        validateWeighmentTotals(request.getTotals());

        String transactionKey = WeighmentSubmitKeys.transactionKey(scheduleId);

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

    private void validateWeighmentTotals(SubmitCompletedWeighmentRequest.WeighmentTotals totals) {
        if (totals == null) {
            throw new BusinessValidationException(ErrorCodes.VALIDATION_PENDING, "Weighment totals are required");
        }
        if (totals.getTotalBirds() == null || totals.getTotalCagesOrBatches() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "totals.totalBirds and totals.totalCagesOrBatches are required");
        }
        if (totals.getTotalGross() == null || totals.getTotalNet() == null || totals.getTotalTare() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "totals totalGross, totalNet, and totalTare are required");
        }
    }

    private void validateWeighmentEntries(String scheduleId, List<WeighmentEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        for (WeighmentEntry entry : entries) {
            WeighmentEntryCategory.validateIfPresent(entry.getSequence(), entry.getCategory());
            if (apiV2Properties.isLiveReadingsRequired()
                    || entry.getLiveReadings() != null
                    || entry.getLiveReadingsAverage() != null) {
                LiveReadingValidation.validateEntry(
                        entry.getSequence(), entry.getLiveReadings(), entry.getLiveReadingsAverage());
            }
        }
    }

    private void validateSubmissionEvidence(
            String saleType, String scheduleId, SubmitCompletedWeighmentRequest request) {
        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        if (farm == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Farm validation evidence is required");
        }
        if (scheduleWorkflowService.isLiftingStepEnabled(saleType, scheduleId, WeighmentWorkflowStep.OTP_VERIFICATION)
                && !Boolean.TRUE.equals(farm.getFarmerOtpVerified())) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "Farmer OTP verification must be completed before submission");
        }
        if (scheduleWorkflowService.isLiftingStepEnabled(saleType, scheduleId, WeighmentWorkflowStep.GEOFENCING)
                && !Boolean.TRUE.equals(farm.getGeofencePassed())) {
            throw new BusinessValidationException(
                    ErrorCodes.BUSINESS_VALIDATION_FAILED, "Geofence validation failed");
        }
    }

    /**
     * Align lifting workflow flags with schedule detail: use request {@code saleType} when sent, otherwise ERP order
     * source via {@link ScheduleRetailEnrichmentService#resolveSaleType}.
     */
    private String resolveSubmissionSaleType(LiftingSchedule schedule, SubmitCompletedWeighmentRequest request) {
        String fromRequest = request.effectiveSaleType();
        if (fromRequest != null && !fromRequest.isBlank()) {
            return fromRequest;
        }
        return retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
    }

    /**
     * RETAIL lifting submissions must include healthy bird count and feed stock (amount + unit). Wholesale may omit.
     */
    private void validateRetailLiftingFarmMetrics(String saleType, SubmitCompletedWeighmentRequest request) {
        if (saleType == null || !"RETAIL".equalsIgnoreCase(saleType.trim())) {
            return;
        }
        if (request.getHealthyBirdsCount() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "healthyBirdsCount is required for RETAIL lifting completion");
        }
        if (request.getHealthyBirdsCount() < 0) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "healthyBirdsCount must be zero or positive");
        }
        FeedStockInfo feed = request.getFeedStock();
        if (feed == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "feedStock is required for RETAIL lifting completion");
        }
        if (feed.getAmount() == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING, "feedStock.amount is required for RETAIL lifting completion");
        }
        if (feed.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException(ErrorCodes.VALIDATION_PENDING, "feedStock.amount must be non-negative");
        }
        if (feed.getUnit() == null || feed.getUnit().isBlank()) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_PENDING,
                    "feedStock.unit is required (e.g. KG or BAG) for RETAIL lifting completion");
        }
        String unit = feed.getUnit().trim();
        if (unit.length() > 32) {
            throw new BusinessValidationException(ErrorCodes.VALIDATION_PENDING, "feedStock.unit must be at most 32 characters");
        }
        feed.setUnit(unit.toUpperCase(Locale.ROOT));
    }
}
