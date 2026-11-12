package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.integration.oracle.ItemGstSqlClient;
import com.suguna.weighment_api_service.integration.oracle.ItemGstSqlClient.ItemGstInfo;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OracleItemGstRepository implements ItemGstRepository {

    private final ItemGstSqlClient sqlClient;

    public OracleItemGstRepository(ItemGstSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Override
    public Optional<ItemGstInfo> find(
            String itemSegment1, Long organizationId, String branchCode, String breedOrDescription) {
        return sqlClient.find(itemSegment1, organizationId, branchCode, breedOrDescription);
    }
}
