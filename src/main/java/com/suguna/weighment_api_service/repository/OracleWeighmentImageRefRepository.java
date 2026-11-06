package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.integration.oracle.WeighmentImageRefSqlClient;
import com.suguna.weighment_api_service.integration.oracle.WeighmentImageRefSqlClient.WeighmentImageRefInsert;
import org.springframework.stereotype.Repository;

@Repository
public class OracleWeighmentImageRefRepository implements WeighmentImageRefRepository {

    private final WeighmentImageRefSqlClient sqlClient;

    public OracleWeighmentImageRefRepository(WeighmentImageRefSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Override
    public void save(WeighmentImageRefInsert row) {
        sqlClient.insertImageRef(row);
    }
}
