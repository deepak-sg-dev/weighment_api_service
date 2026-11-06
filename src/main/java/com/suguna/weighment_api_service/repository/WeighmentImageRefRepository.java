package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.integration.oracle.WeighmentImageRefSqlClient.WeighmentImageRefInsert;

public interface WeighmentImageRefRepository {

    void save(WeighmentImageRefInsert row);
}
