package com.suguna.weighment_api_service.integration.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;
import com.suguna.weighment_api_service.util.OracleJsonMapper;

final class WeighmentDtlHistoryJson {

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = OracleJsonMapper.INSTANCE;

    private WeighmentDtlHistoryJson() {}

    static String entryJson(
            SubmitCompletedWeighmentRequest request, WeighmentEntry entry, boolean includeHeaderPayload) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("sequence", entry.getSequence());
            root.put("category", entry.getCategory());
            if (entry.getBirdsApplied() != null) {
                root.put("birdsApplied", entry.getBirdsApplied());
            }
            if (entry.getGross() != null) {
                root.put("gross", entry.getGross());
            }
            if (entry.getNet() != null) {
                root.put("net", entry.getNet());
            }
            if (entry.getAverageBirdWeight() != null) {
                root.put("averageBirdWeight", entry.getAverageBirdWeight());
            }
            root.put("weightDifference", entry.getWeightDifference());
            root.put("status", entry.getStatus());
            if (entry.getStartTime() != null) {
                root.put("startTime", entry.getStartTime().toString());
            }
            if (entry.getEndTime() != null) {
                root.put("endTime", entry.getEndTime().toString());
            }
            if (includeHeaderPayload) {
                root.put("deviceId", request.getDeviceId());
                root.put("supervisorId", request.getSupervisorId());
                root.put("saleType", request.effectiveSaleType());
                root.put("birdType", request.effectiveBirdType());
                if (request.getPlannedQuantity() != null) {
                    root.put("plannedQuantity", request.getPlannedQuantity());
                }
                root.set("scale", MAPPER.valueToTree(request.getScale()));
                root.set("farmValidation", MAPPER.valueToTree(request.getFarmValidation()));
                root.set("vehicle", MAPPER.valueToTree(request.getVehicle()));
                root.set("totals", MAPPER.valueToTree(request.getTotals()));
                root.set("completion", MAPPER.valueToTree(request.getCompletion()));
                root.set("audit", MAPPER.valueToTree(request.getAudit()));
            }
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":true}";
        }
    }
}
