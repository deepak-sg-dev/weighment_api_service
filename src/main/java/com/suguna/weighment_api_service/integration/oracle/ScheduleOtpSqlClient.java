package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ScheduleOtpSqlClient {

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;

    public ScheduleOtpSqlClient(JdbcTemplate jdbcTemplate, OracleErpProperties erpProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
    }

    public Optional<ScheduleOtpRow> findOtpContext(String supervisorCode, String scheduleId) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        List<String> supervisorKeys = LiftingScheduleSqlClient.supervisorBindKeys(supervisorCode);
        String sql =
                """
                SELECT o.farmer_otp,
                       o.trader_otp,
                       o.farmer_mobile_no,
                       o.customercode
                  FROM sug_mai_birds_lifting_order o
                 WHERE """
                        + supervisorEqualityPredicate(supervisorKeys)
                        + """
                           AND NVL(o.application, ?) = ?
                           AND """
                        + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH;

        List<Object> args = new ArrayList<>(supervisorKeys);
        args.add(erpProperties.getApplicationCode());
        args.add(erpProperties.getApplicationCode());
        args.add(normalizedScheduleId);
        args.add(normalizedScheduleId);

        List<ScheduleOtpRow> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ScheduleOtpRow(
                        rs.getObject("farmer_otp"),
                        rs.getObject("trader_otp"),
                        rs.getString("farmer_mobile_no"),
                        rs.getString("customercode")),
                args.toArray());
        return rows.stream().findFirst();
    }

    private static String supervisorEqualityPredicate(List<String> keys) {
        if (keys.size() == 1) {
            return " o.mobileuserempid = ?";
        }
        return " (o.mobileuserempid = ? OR o.mobileuserempid = ?)";
    }

    public record ScheduleOtpRow(Object farmerOtp, Object traderOtp, String farmerMobile, String traderId) {

        public boolean isFarmerOtpRequired() {
            return otpConfigured(farmerOtp);
        }

        public boolean isTraderOtpRequired() {
            return otpConfigured(traderOtp);
        }

        public boolean farmerOtpMatches(String enteredOtp) {
            return otpMatches(farmerOtp, enteredOtp);
        }

        public boolean traderOtpMatches(String enteredOtp) {
            return otpMatches(traderOtp, enteredOtp);
        }

        public String farmerOtpValue() {
            return isFarmerOtpRequired() ? normalizeOtp(farmerOtp) : null;
        }

        public String traderOtpValue() {
            return isTraderOtpRequired() ? normalizeOtp(traderOtp) : null;
        }

        private static boolean otpConfigured(Object stored) {
            if (stored == null) {
                return false;
            }
            if (stored instanceof Number number) {
                return number.longValue() > 0L;
            }
            String text = stored.toString().trim();
            return !text.isEmpty() && !"0".equals(text);
        }

        private static boolean otpMatches(Object stored, String enteredOtp) {
            if (!otpConfigured(stored) || enteredOtp == null) {
                return false;
            }
            String expected = normalizeOtp(stored);
            String actual = enteredOtp.trim().replaceAll("\\D", "");
            return expected.equals(actual);
        }

        private static String normalizeOtp(Object stored) {
            if (stored instanceof Number number) {
                return String.valueOf(number.longValue());
            }
            return stored.toString().trim().replaceAll("\\D", "");
        }
    }
}
