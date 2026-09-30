package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ApiResponseProperties.class,
    JwtProperties.class,
    OracleLiftingProperties.class,
    OracleErpProperties.class
})
public class ApiWebConfig {
}
