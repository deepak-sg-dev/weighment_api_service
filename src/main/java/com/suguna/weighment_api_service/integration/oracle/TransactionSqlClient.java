package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionSqlClient {

    private static final String ACKNOWLEDGEMENT_SQL =
            """
            SELECT TRANS_ID,
                   STATUS,
                   REMARKS,
                   UPDATED_DATE
            FROM SUG_MAI_BIRDS_WEIGHMENT
            WHERE TRANS_ID = ?
            """;

    private static final String RETRY_SQL =
            """
            UPDATE SUG_MAI_BIRDS_WEIGHMENT
            SET ERRORCOUNT = 0,
                ABORTCOUNT = 0,
                UPDATED_DATE = SYSDATE
            WHERE TRANS_ID = ?
            """;

    private static final String CORRECTION_SQL =
            """
            UPDATE SUG_MAI_BIRDS_WEIGHMENT
            SET REMARKS = ?,
                UPDATED_DATE = SYSDATE
            WHERE TRANS_ID = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public TransactionSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TransactionAcknowledgementResponse
    getAcknowledgement(String transactionId) {

        return jdbcTemplate.queryForObject(
                ACKNOWLEDGEMENT_SQL,
                (rs, rowNum) ->
                        TransactionAcknowledgementResponse.builder()
                                .success(true)
                                .transactionId(
                                        rs.getString("TRANS_ID"))
                                .status(
                                        rs.getString("STATUS"))
                                .message(
                                        rs.getString("REMARKS"))
                                .acknowledgedAt(
                                        rs.getString("UPDATED_DATE"))
                                .build(),
                transactionId
        );
    }

    public void retryTransaction(
            String transactionId,
            TransactionRetryRequest request) {

        jdbcTemplate.update(
                RETRY_SQL,
                transactionId
        );
    }

    public void submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request) {

        jdbcTemplate.update(
                CORRECTION_SQL,
                request.getCorrections().getRemarks(),
                transactionId
        );
    }
}