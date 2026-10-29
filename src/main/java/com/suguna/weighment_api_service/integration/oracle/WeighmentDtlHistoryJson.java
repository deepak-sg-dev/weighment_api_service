package com.suguna.weighment_api_service.integration.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
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
            if (entry.getSequence() != null) {
                root.put("sequence", entry.getSequence());
            }
            if (entry.getCategory() != null) {
                root.put("category", entry.getCategory());
            }
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
            if (entry.getWeightDifference() != null) {
                root.put("weightDifference", entry.getWeightDifference());
            }
            if (entry.getStatus() != null) {
                root.put("status", entry.getStatus());
            }
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

    static String deliveryEntryJson(
            SubmitDeliveryWeighmentRequest request, WeighmentEntry entry, boolean includeHeaderPayload) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            if (entry.getSequence() != null) {
                root.put("sequence", entry.getSequence());
            }
            if (entry.getCategory() != null) {
                root.put("category", entry.getCategory());
            }
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
            if (entry.getWeightDifference() != null) {
                root.put("weightDifference", entry.getWeightDifference());
            }
            if (entry.getStatus() != null) {
                root.put("status", entry.getStatus());
            }
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
                root.put("scheduleId", request.getScheduleId());
                root.put("orderId", request.getOrderId());
                root.put("operationType", request.getOperationType());
                if (request.getPlannedQuantity() != null) {
                    root.put("plannedQuantity", request.getPlannedQuantity());
                }
                root.set("scale", MAPPER.valueToTree(request.getScale()));
                root.set("shopValidation", MAPPER.valueToTree(request.getShopValidation()));
                root.set("customer", MAPPER.valueToTree(request.getCustomer()));
                root.set("totals", MAPPER.valueToTree(request.getTotals()));
                root.set("payment", MAPPER.valueToTree(request.getPayment()));
                root.set("shopStock", MAPPER.valueToTree(request.getShopStock()));
                root.set("timeline", MAPPER.valueToTree(request.getTimeline()));
                root.set("audit", MAPPER.valueToTree(request.getAudit()));
            }
            return MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":true}";
        }
    }
}
