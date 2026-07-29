package com.urbanhub.ingestion.api;

import com.urbanhub.ingestion.application.IngestionResult;
import com.urbanhub.ingestion.application.MeasurementIngestionService;
import com.urbanhub.ingestion.security.ApiKeyAuthenticationEntryPoint;
import com.urbanhub.ingestion.security.ApiKeyAuthenticationFilter;
import com.urbanhub.ingestion.security.RateLimitFilter;
import com.urbanhub.ingestion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@Import({
        SecurityConfig.class,
        ApiKeyAuthenticationFilter.class,
        ApiKeyAuthenticationEntryPoint.class,
        RateLimitFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "urbanhub.security.ingestion-api-key=test-api-key",
        "urbanhub.security.rate-limit.capacity=100",
        "urbanhub.security.rate-limit.refill-tokens=100",
        "urbanhub.security.rate-limit.refill-duration-seconds=60"
})
class IngestionPerformanceTest {

    private static final String ENDPOINT =
            "/api/ingestion/measurements";

    private static final long MAX_RESPONSE_TIME_MILLIS = 1_000;

    private static final String VALID_REQUEST = """
            {
              "zoneId": "ZFE-1",
              "stationId": "AIR-STATION-042",
              "indicator": "NO2",
              "value": 220.5,
              "timestamp": "2026-07-27T08:00:00Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementIngestionService ingestionService;

    @Test
    void ingestionEndpointShouldRespondWithinOneSecond()
            throws Exception {

        when(ingestionService.ingest(any()))
                .thenReturn(new IngestionResult(
                        "ACCEPTED",
                        "corr-performance-test"
                ));

        /*
         * Requête de chauffe pour éviter de mesurer
         * l’initialisation paresseuse de certains composants.
         */
        performRequest();

        long startNanos = System.nanoTime();

        performRequest();

        long durationMillis =
                (System.nanoTime() - startNanos) / 1_000_000;

        assertTrue(
                durationMillis < MAX_RESPONSE_TIME_MILLIS,
                () -> "Expected response time below "
                        + MAX_RESPONSE_TIME_MILLIS
                        + " ms, but observed "
                        + durationMillis
                        + " ms"
        );
    }

    private void performRequest() throws Exception {
        mockMvc.perform(
                        post(ENDPOINT)
                                .header(
                                        "X-API-Key",
                                        "test-api-key"
                                )
                                .contentType("application/json")
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isAccepted());
    }
}
