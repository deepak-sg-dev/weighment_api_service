package com.suguna.weighment_api_service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Shared JSON mapper for JDBC ext_json (matches {@link com.suguna.weighment_api_service.config.JacksonConfig}). */
public final class OracleJsonMapper {

    public static final ObjectMapper INSTANCE = create();

    private OracleJsonMapper() {}

    private static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
