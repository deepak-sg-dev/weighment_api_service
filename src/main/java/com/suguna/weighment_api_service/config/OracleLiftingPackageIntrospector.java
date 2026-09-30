package com.suguna.weighment_api_service.config;

import com.suguna.weighment_api_service.integration.oracle.BirdsLiftingPackageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.oracle.lifting.introspect-on-startup", havingValue = "true")
public class OracleLiftingPackageIntrospector implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OracleLiftingPackageIntrospector.class);

    private final BirdsLiftingPackageClient packageClient;

    public OracleLiftingPackageIntrospector(BirdsLiftingPackageClient packageClient) {
        this.packageClient = packageClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Introspecting Oracle lifting package procedures...");
        packageClient.logPackageProcedures();
    }
}
