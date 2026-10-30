package com.suguna.weighment_api_service.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.suguna.weighment_api_service.util.OracleJsonMapper;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;
import com.suguna.weighment_api_service.dto.transaction.WeighmentSubmissionDetailResponse;
import com.suguna.weighment_api_service.dto.transaction.WeighmentSubmissionHistoryResponse;
import com.suguna.weighment_api_service.dto.transaction.WeighmentSubmissionHistoryResponse.WeighmentHistoryLineDto;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient.SubmissionHeaderSnapshot;
import com.suguna.weighment_api_service.integration.oracle.WeighmentDetailSqlClient.StoredWeighmentDetailLine;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeighmentSubmissionMapper {

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = OracleJsonMapper.INSTANCE;

    public WeighmentSubmissionDetailResponse toDetailResponse(
            String scheduleId,
            SubmissionHeaderSnapshot header,
            List<StoredWeighmentDetailLine> lines) {
        WeighmentSubmissionDetailResponse response = new WeighmentSubmissionDetailResponse();
        response.setScheduleId(scheduleId);
        response.setTransactionId(String.valueOf(header.transId()));
        response.setStatus(TransactionSqlClient.normalizeStatusPublic(header.status()));
        response.setReceivedAt(TransactionSqlClient.formatInstantPublic(header.updatedDate()));
        response.setMessage("Weighment submission loaded");
        response.setSubmission(rebuildSubmission(scheduleId, header, lines));
        return response;
    }

    public WeighmentSubmissionHistoryResponse toHistoryResponse(
            String scheduleId, SubmissionHeaderSnapshot header, List<StoredWeighmentDetailLine> lines) {
        WeighmentSubmissionHistoryResponse response = new WeighmentSubmissionHistoryResponse();
        response.setScheduleId(scheduleId);
        response.setTransactionId(String.valueOf(header.transId()));
        List<WeighmentHistoryLineDto> historyLines = new ArrayList<>();
        for (StoredWeighmentDetailLine line : lines) {
            historyLines.add(toHistoryLine(line));
        }
        response.setLines(historyLines);
        return response;
    }

    private WeighmentHistoryLineDto toHistoryLine(StoredWeighmentDetailLine line) {
        WeighmentHistoryLineDto dto = new WeighmentHistoryLineDto();
        dto.setDtlId(line.dtlId());
        dto.setSequence(line.lineSequence());
        dto.setCategory(line.category());
        dto.setBirdsApplied(line.birdsApplied());
        dto.setTareWeight(line.tareWt());
        dto.setGross(line.grossWt());
        dto.setNet(line.netWt());
        dto.setAverageBirdWeight(line.avgBirdWt());
        dto.setWeightDifference(line.weightDifference());
        dto.setStatus(mapEntryStatus(line.rowStatus()));
        dto.setStartTime(TransactionSqlClient.formatInstantPublic(line.capturedAt()));
        dto.setEndTime(TransactionSqlClient.formatInstantPublic(line.eventAt()));
        dto.setCaptureLatitude(line.captureLat());
        dto.setCaptureLongitude(line.captureLong());
        dto.setCaptureAccuracyMeters(line.captureAccuracyM());
        dto.setScaleDeviceId(line.scaleNo());
        dto.setDeviceId(line.deviceId());
        dto.setRowStatus(line.rowStatus());
        dto.setRecordedAt(TransactionSqlClient.formatInstantPublic(line.creationDate()));
        return dto;
    }

    private SubmitCompletedWeighmentRequest rebuildSubmission(
            String scheduleId,
            SubmissionHeaderSnapshot header,
            List<StoredWeighmentDetailLine> lines) {
        SubmitCompletedWeighmentRequest request = new SubmitCompletedWeighmentRequest();
        request.setScheduleId(scheduleId);

        JsonNode headerJson = firstLineExtJson(lines);
        if (headerJson != null) {
            if (headerJson.hasNonNull("deviceId")) {
                request.setDeviceId(headerJson.get("deviceId").asText());
            }
            if (headerJson.hasNonNull("supervisorId")) {
                request.setSupervisorId(headerJson.get("supervisorId").asText());
            }
            if (headerJson.hasNonNull("saleType")) {
                request.setSaleType(headerJson.get("saleType").asText());
            }
            if (headerJson.hasNonNull("birdType")) {
                request.setBirdType(headerJson.get("birdType").asText());
            }
            if (headerJson.has("plannedQuantity") && !headerJson.get("plannedQuantity").isNull()) {
                request.setPlannedQuantity(headerJson.get("plannedQuantity").asDouble());
            }
            request.setScale(readTree(headerJson.get("scale"), SubmitCompletedWeighmentRequest.ScaleInfo.class));
            request.setFarmValidation(
                    readTree(headerJson.get("farmValidation"), SubmitCompletedWeighmentRequest.FarmValidation.class));
            request.setVehicle(
                    readTree(headerJson.get("vehicle"), SubmitCompletedWeighmentRequest.VehicleInfo.class));
            request.setCompletion(
                    readTree(headerJson.get("completion"), SubmitCompletedWeighmentRequest.CompletionInfo.class));
            request.setAudit(readTree(headerJson.get("audit"), SubmitCompletedWeighmentRequest.AuditInfo.class));
        }

        if (request.getDeviceId() == null && !lines.isEmpty()) {
            request.setDeviceId(lines.get(0).deviceId());
        }
        if (request.getBirdType() == null && header.birdType() != null) {
            request.setBirdType(header.birdType());
        }
        if (request.getPlannedQuantity() == null && header.orderedQty() != null) {
            request.setPlannedQuantity(header.orderedQty());
        }

        request.setTotals(rebuildTotals(header));
        mergeCompletionFromHeader(request, header);
        mergeFarmCaptureFromHeader(request, header);
        if (request.getVehicle() == null && header.vehicleNo() != null) {
            SubmitCompletedWeighmentRequest.VehicleInfo vehicle = new SubmitCompletedWeighmentRequest.VehicleInfo();
            vehicle.setRegistrationNumber(header.vehicleNo());
            request.setVehicle(vehicle);
        }
        if (request.getScale() == null && header.scaleNo() != null) {
            SubmitCompletedWeighmentRequest.ScaleInfo scale = new SubmitCompletedWeighmentRequest.ScaleInfo();
            scale.setDeviceId(header.scaleNo());
            request.setScale(scale);
        }

        List<WeighmentEntry> entries = new ArrayList<>();
        for (StoredWeighmentDetailLine line : lines) {
            entries.add(rebuildEntry(line));
        }
        request.setWeighmentEntries(entries);
        return request;
    }

    private SubmitCompletedWeighmentRequest.WeighmentTotals rebuildTotals(SubmissionHeaderSnapshot header) {
        SubmitCompletedWeighmentRequest.WeighmentTotals totals = new SubmitCompletedWeighmentRequest.WeighmentTotals();
        if (header.noOfCages() != null) {
            totals.setTotalCagesOrBatches(header.noOfCages());
        }
        if (header.noOfBirds() != null) {
            totals.setTotalBirds(header.noOfBirds());
        }
        totals.setTotalTare(header.emptyWt());
        totals.setTotalGross(header.grossWt());
        totals.setTotalNet(header.netWt());
        totals.setOverallAverageBirdWeight(header.avgWt());
        return totals;
    }

    private WeighmentEntry rebuildEntry(StoredWeighmentDetailLine line) {
        WeighmentEntry entry = new WeighmentEntry();
        entry.setSequence(line.lineSequence());
        entry.setCategory(line.category());
        entry.setBirdsApplied(line.birdsApplied());
        entry.setGross(line.grossWt());
        entry.setNet(line.netWt());
        entry.setAverageBirdWeight(line.avgBirdWt());
        entry.setWeightDifference(line.weightDifference());
        entry.setStatus(mapEntryStatus(line.rowStatus()));
        if (line.capturedAt() != null) {
            entry.setStartTime(line.capturedAt().toInstant());
        }
        if (line.eventAt() != null) {
            entry.setEndTime(line.eventAt().toInstant());
        }
        JsonNode ext = parseExt(line.extJson());
        if (ext != null) {
            if (ext.hasNonNull("status")) {
                entry.setStatus(ext.get("status").asText());
            }
            if (ext.has("startTime") && !ext.get("startTime").isNull()) {
                entry.setStartTime(Instant.parse(ext.get("startTime").asText()));
            }
            if (ext.has("endTime") && !ext.get("endTime").isNull()) {
                entry.setEndTime(Instant.parse(ext.get("endTime").asText()));
            }
        }
        return entry;
    }

    private static JsonNode firstLineExtJson(List<StoredWeighmentDetailLine> lines) {
        if (lines.isEmpty()) {
            return null;
        }
        return parseExt(lines.get(0).extJson());
    }

    private static JsonNode parseExt(String extJson) {
        if (extJson == null || extJson.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(extJson);
        } catch (Exception ex) {
            return null;
        }
    }

    private <T> T readTree(JsonNode node, Class<T> type) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return MAPPER.treeToValue(node, type);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String mapEntryStatus(String rowStatus) {
        if (rowStatus == null) {
            return "COMPLETED";
        }
        if ("ABORTED".equalsIgnoreCase(rowStatus.trim())) {
            return "ABORTED";
        }
        return "COMPLETED";
    }

    private static void mergeCompletionFromHeader(
            SubmitCompletedWeighmentRequest request, SubmissionHeaderSnapshot header) {
        SubmitCompletedWeighmentRequest.CompletionInfo completion = request.getCompletion();
        if (completion == null) {
            completion = new SubmitCompletedWeighmentRequest.CompletionInfo();
            request.setCompletion(completion);
        }
        if (completion.getWeighingStartTime() == null && header.wmStartTime() != null) {
            completion.setWeighingStartTime(header.wmStartTime().toInstant());
        }
        if (completion.getOutTime() == null && header.vehicleOuttime() != null) {
            completion.setOutTime(header.vehicleOuttime().toInstant());
        } else if (completion.getOutTime() == null && header.wmEndTime() != null) {
            completion.setOutTime(header.wmEndTime().toInstant());
        }
    }

    private static void mergeFarmCaptureFromHeader(
            SubmitCompletedWeighmentRequest request, SubmissionHeaderSnapshot header) {
        if (header.latitude() == null && header.longitude() == null) {
            return;
        }
        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        if (farm == null) {
            farm = new SubmitCompletedWeighmentRequest.FarmValidation();
            request.setFarmValidation(farm);
        }
        if (farm.getCapturedLatitude() == null) {
            farm.setCapturedLatitude(header.latitude());
        }
        if (farm.getCapturedLongitude() == null) {
            farm.setCapturedLongitude(header.longitude());
        }
    }
}
