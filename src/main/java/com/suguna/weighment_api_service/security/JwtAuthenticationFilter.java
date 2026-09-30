package com.suguna.weighment_api_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Registered only via {@link com.suguna.weighment_api_service.config.SecurityConfig}
 * (not a {@code @Component}) so it is not auto-registered on the servlet filter chain twice.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String REQUEST_AUTH_FAILURE = "weighment.auth.failure";

    public static final String FAILURE_MISSING = "MISSING";
    public static final String FAILURE_MALFORMED = "MALFORMED";
    public static final String FAILURE_INVALID = "INVALID";

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_ACCESS_TOKEN = "X-Access-Token";
    private static final String HEADER_ACCESS_TOKEN_ALT = "accessToken";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Optional<String> tokenOptional = resolveAccessToken(request);
        if (tokenOptional.isEmpty()) {
            if (hasNonBlankHeader(request, HttpHeaders.AUTHORIZATION)
                    || hasNonBlankHeader(request, HEADER_ACCESS_TOKEN)
                    || hasNonBlankHeader(request, HEADER_ACCESS_TOKEN_ALT)) {
                request.setAttribute(REQUEST_AUTH_FAILURE, FAILURE_MALFORMED);
            } else {
                request.setAttribute(REQUEST_AUTH_FAILURE, FAILURE_MISSING);
            }
        } else {
            String token = tokenOptional.get();
            Optional<AuthenticatedSupervisor> authenticated = jwtService.parseToken(token);
            if (authenticated.isPresent()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        authenticated.get(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                request.setAttribute(REQUEST_AUTH_FAILURE, FAILURE_INVALID);
            }
        }
        filterChain.doFilter(request, response);
    }

    static Optional<String> resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String bearerToken = extractBearerToken(authorization);
        if (bearerToken != null) {
            return Optional.of(bearerToken);
        }
        if (authorization != null && !authorization.isBlank() && looksLikeJwt(authorization.trim())) {
            return Optional.of(authorization.trim());
        }
        String headerToken = firstNonBlank(
                request.getHeader(HEADER_ACCESS_TOKEN), request.getHeader(HEADER_ACCESS_TOKEN_ALT));
        if (headerToken != null) {
            String normalized = extractBearerToken(headerToken);
            if (normalized != null) {
                return Optional.of(normalized);
            }
            if (looksLikeJwt(headerToken.trim())) {
                return Optional.of(headerToken.trim());
            }
        }
        return Optional.empty();
    }

    private static boolean hasNonBlankHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value != null && !value.isBlank();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }

    private static String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String trimmed = authorization.trim();
        if (trimmed.length() <= BEARER_PREFIX.length()
                || !trimmed.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = trimmed.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static boolean looksLikeJwt(String value) {
        return value.startsWith("eyJ") && value.chars().filter(ch -> ch == '.').count() == 2;
    }
}
