package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.config.JwtProperties;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final String CLAIM_DEVICE_ID = "deviceId";
    private static final String CLAIM_SUPERVISOR_CODE = "supervisorCode";
    private static final String CLAIM_SUPERVISOR_NAME = "supervisorName";
    private static final String CLAIM_SUPERVISOR_MOBILE = "supervisorMobile";
    private static final String CLAIM_BRANCH_ID = "branchId";
    private static final String CLAIM_BRANCH_NAME = "branchName";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = resolveKey(jwtProperties.getSecretKey());
    }

    public String generateAccessToken(String deviceId, SupervisorProfile supervisor) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getExpirationTime());
        return Jwts.builder()
                .subject(supervisor.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_DEVICE_ID, deviceId)
                .claim(CLAIM_SUPERVISOR_CODE, supervisor.getCode())
                .claim(CLAIM_SUPERVISOR_NAME, supervisor.getName())
                .claim(CLAIM_SUPERVISOR_MOBILE, supervisor.getMobile())
                .claim(CLAIM_BRANCH_ID, supervisor.getBranchId())
                .claim(CLAIM_BRANCH_NAME, supervisor.getBranchName())
                .signWith(secretKey)
                .compact();
    }

    public Optional<AuthenticatedSupervisor> parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            SupervisorProfile supervisor = SupervisorProfile.builder()
                    .id(claims.getSubject())
                    .code(claims.get(CLAIM_SUPERVISOR_CODE, String.class))
                    .name(claims.get(CLAIM_SUPERVISOR_NAME, String.class))
                    .mobile(claims.get(CLAIM_SUPERVISOR_MOBILE, String.class))
                    .branchId(claims.get(CLAIM_BRANCH_ID, String.class))
                    .branchName(claims.get(CLAIM_BRANCH_NAME, String.class))
                    .active(true)
                    .build();
            String deviceId = claims.get(CLAIM_DEVICE_ID, String.class);
            return Optional.of(new AuthenticatedSupervisor(deviceId, supervisor));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private static SecretKey resolveKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret-key must be configured");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
