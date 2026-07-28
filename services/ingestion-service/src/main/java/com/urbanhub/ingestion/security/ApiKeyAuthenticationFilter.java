package com.urbanhub.ingestion.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String INGESTION_PATH =
            "/api/ingestion/measurements";

    private final byte[] expectedApiKey;

    public ApiKeyAuthenticationFilter(
            @Value("${urbanhub.security.ingestion-api-key}")
            String expectedApiKey
    ) {
        if (expectedApiKey == null || expectedApiKey.isBlank()) {
            throw new IllegalStateException(
                    "urbanhub.security.ingestion-api-key must be configured"
            );
        }

        this.expectedApiKey =
                expectedApiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        return !INGESTION_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String providedApiKey =
                request.getHeader(API_KEY_HEADER);

        if (!isValidApiKey(providedApiKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        ApiKeyAuthenticationToken authentication =
                new ApiKeyAuthenticationToken("iot-sensor");

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isValidApiKey(String providedApiKey) {
        if (providedApiKey == null || providedApiKey.isBlank()) {
            return false;
        }

        byte[] providedBytes =
                providedApiKey.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(
                expectedApiKey,
                providedBytes
        );
    }
}