package com.urbanhub.ingestion.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanhub.ingestion.api.ApiErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String INGESTION_PATH =
            "/api/ingestion/measurements";

    private static final String REMAINING_HEADER =
            "X-Rate-Limit-Remaining";

    private static final String RETRY_AFTER_HEADER =
            "Retry-After";

    private final Map<String, Bucket> buckets =
            new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final long capacity;
    private final long refillTokens;
    private final Duration refillDuration;

    public RateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${urbanhub.security.rate-limit.capacity}")
            long capacity,
            @Value("${urbanhub.security.rate-limit.refill-tokens}")
            long refillTokens,
            @Value("${urbanhub.security.rate-limit.refill-duration-seconds}")
            long refillDurationSeconds
    ) {
        validateConfiguration(
                capacity,
                refillTokens,
                refillDurationSeconds
        );

        this.objectMapper = objectMapper;
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration =
                Duration.ofSeconds(refillDurationSeconds);
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

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        /*
         * Une clé absente ou invalide reste gérée par Spring Security.
         * Le rate limiting ne s'applique qu'à un appel authentifié.
         */
        if (authentication == null
                || !authentication.isAuthenticated()) {

            filterChain.doFilter(request, response);
            return;
        }

        String clientIdentity =
                authentication.getName();

        Bucket bucket = buckets.computeIfAbsent(
                clientIdentity,
                ignored -> createBucket()
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader(
                REMAINING_HEADER,
                String.valueOf(probe.getRemainingTokens())
        );

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        writeRateLimitError(
                request,
                response,
                probe
        );
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(
                        refillTokens,
                        refillDuration
                )
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void writeRateLimitError(
            HttpServletRequest request,
            HttpServletResponse response,
            ConsumptionProbe probe
    ) throws IOException {

        long retryAfterSeconds = Math.max(
                1,
                (long) Math.ceil(
                        probe.getNanosToWaitForRefill()
                                / 1_000_000_000.0
                )
        );

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "RATE_LIMIT_EXCEEDED",
                        "Too many ingestion requests",
                        request.getRequestURI(),
                        Map.of()
                );

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                RETRY_AFTER_HEADER,
                String.valueOf(retryAfterSeconds)
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }

    private void validateConfiguration(
            long capacity,
            long refillTokens,
            long refillDurationSeconds
    ) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "rate-limit capacity must be greater than zero"
            );
        }

        if (refillTokens <= 0) {
            throw new IllegalArgumentException(
                    "rate-limit refill-tokens must be greater than zero"
            );
        }

        if (refillDurationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "rate-limit refill duration must be greater than zero"
            );
        }
    }
}
