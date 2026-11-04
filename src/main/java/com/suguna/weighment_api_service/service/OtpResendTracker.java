package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.ApiV2Properties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.dto.weighment.otp.PartyType;
import com.suguna.weighment_api_service.exception.ApiException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OtpResendTracker {

    private final ApiV2Properties apiV2Properties;
    private final Map<String, ResendState> states = new ConcurrentHashMap<>();

    public OtpResendTracker(ApiV2Properties apiV2Properties) {
        this.apiV2Properties = apiV2Properties;
    }

    public int recordResend(String scheduleId, PartyType partyType, String deliveryId) {
        String key = key(scheduleId, partyType, deliveryId);
        Instant now = Instant.now();
        ResendState state = states.compute(key, (k, existing) -> {
            if (existing == null) {
                return new ResendState(1, now);
            }
            if (existing.lastSent() != null) {
                long elapsed = now.getEpochSecond() - existing.lastSent().getEpochSecond();
                if (elapsed < apiV2Properties.getOtpResendCooldownSeconds()) {
                    throw new ApiException(
                            ErrorCode.TOO_MANY_REQUESTS,
                            "OTP resend is too soon; wait "
                                    + (apiV2Properties.getOtpResendCooldownSeconds() - elapsed)
                                    + " seconds");
                }
            }
            int next = existing.count() + 1;
            if (next > apiV2Properties.getOtpResendMax()) {
                throw new ApiException(ErrorCode.TOO_MANY_REQUESTS, "OTP resend limit reached");
            }
            return new ResendState(next, now);
        });
        return state.count();
    }

    public int currentCount(String scheduleId, PartyType partyType, String deliveryId) {
        ResendState state = states.get(key(scheduleId, partyType, deliveryId));
        return state == null ? 0 : state.count();
    }

    private static String key(String scheduleId, PartyType partyType, String deliveryId) {
        return scheduleId + "|" + partyType + "|" + (deliveryId == null ? "" : deliveryId.trim());
    }

    private record ResendState(int count, Instant lastSent) {}
}
