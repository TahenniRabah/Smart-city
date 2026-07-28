package com.urbanhub.ingestion.security;

import com.urbanhub.ingestion.api.GlobalExceptionHandler;
import com.urbanhub.ingestion.api.IngestionController;
import com.urbanhub.ingestion.application.IngestionResult;
import com.urbanhub.ingestion.application.MeasurementIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        "urbanhub.security.rate-limit.capacity=2",
        "urbanhub.security.rate-limit.refill-tokens=2",
        "urbanhub.security.rate-limit.refill-duration-seconds=60"
})
class RateLimitFilterTest {

    private static final String ENDPOINT =
            "/api/ingestion/measurements";

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
    void shouldRejectRequestWhenRateLimitIsExceeded()
            throws Exception {

        when(ingestionService.ingest(any()))
                .thenReturn(new IngestionResult(
                        "ACCEPTED",
                        "corr-rate-limit-test"
                ));

        performValidRequest()
                .andExpect(status().isAccepted())
                .andExpect(header().string(
                        "X-Rate-Limit-Remaining",
                        "1"
                ));

        performValidRequest()
                .andExpect(status().isAccepted())
                .andExpect(header().string(
                        "X-Rate-Limit-Remaining",
                        "0"
                ));

        performValidRequest()
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error")
                        .value("RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message")
                        .value("Too many ingestion requests"))
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string(
                        "X-Rate-Limit-Remaining",
                        "0"
                ));

        verify(ingestionService, times(2))
                .ingest(any());
    }

    private org.springframework.test.web.servlet.ResultActions
    performValidRequest() throws Exception {

        return mockMvc.perform(
                post(ENDPOINT)
                        .header(
                                "X-API-Key",
                                "test-api-key"
                        )
                        .contentType("application/json")
                        .content(VALID_REQUEST)
        );
    }
}