package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.integration.oracle.ItemGstSqlClient.ItemGstInfo;
import java.util.Optional;

public interface ItemGstRepository {

    Optional<ItemGstInfo> find(
            String itemSegment1, Long organizationId, String branchCode, String breedOrDescription);
}
