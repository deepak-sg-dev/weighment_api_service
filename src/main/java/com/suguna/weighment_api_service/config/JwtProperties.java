package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secretKey;
    private long expirationTime = 900_000L;
    private long refreshExpirationTime = 7_776_000_000L;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public long getRefreshExpirationTime() {
        return refreshExpirationTime;
    }

    public void setRefreshExpirationTime(long refreshExpirationTime) {
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public long getExpiresInSeconds() {
        return Math.max(1L, expirationTime / 1000L);
    }
}
