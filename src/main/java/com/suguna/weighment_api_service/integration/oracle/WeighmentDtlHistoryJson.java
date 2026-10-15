package com.suguna.weighment_api_service.integration.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;

final class WeighmentDtlHistoryJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WeighmentDtlHistoryJson() {}

    static String entryJson(
            SubmitCompletedWeighmentRequest request, WeighmentEntry entry, boolean includeHeaderPayload) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("category", entry.getCategory());
            root.put("weightDifference", entry.getWeightDifference());
            root.put("entryStatus", entry.getStatus());
            if (entry.getStartTime() != null) {
                root.put("startTime", entry.getStartTime().toString());
            }
            if (entry.getEndTime() != null) {
                root.put("endTime", entry.getEndTime().toString());
            }
            if (includeHeaderPayload) {
                root.set("scale", MAPPER.valueToTree(request.getScale()));
                root.set("farmValidation", MAPPER.valueToTree(request.getFarmValidation()));
                root.set("completion", MAPPER.valueToTree(request.getCompletion()));
                root.set("audit", MAPPER.valueToTree(request.getAudit()));
                root.put("saleType", request.effectiveSaleType());
                root.put("birdType", request.effectiveBirdType());
                root.put("plannedQuantity", request.getPlannedQuantity());
            }
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":true}";
        }
    }
}
