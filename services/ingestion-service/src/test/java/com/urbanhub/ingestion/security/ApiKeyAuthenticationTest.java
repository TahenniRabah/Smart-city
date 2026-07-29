package com.urbanhub.ingestion.security;

import com.urbanhub.ingestion.api.IngestionController;
import com.urbanhub.ingestion.application.IngestionResult;
import com.urbanhub.ingestion.application.MeasurementIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@Import({
        SecurityConfig.class,
        ApiKeyAuthenticationFilter.class,
        ApiKeyAuthenticationEntryPoint.class,
        RateLimitFilter.class
})
@TestPropertySource(properties = {
        "urbanhub.security.ingestion-api-key=test-api-key",
        "urbanhub.security.rate-limit.capacity=100",
        "urbanhub.security.rate-limit.refill-tokens=100",
        "urbanhub.security.rate-limit.refill-duration-seconds=60"
})
class ApiKeyAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementIngestionService ingestionService;

    private static final String VALID_REQUEST = """
            {
              "zoneId": "ZFE-1",
              "stationId": "AIR-STATION-042",
              "indicator": "NO2",
              "value": 220.5,
              "timestamp": "2026-05-06T14:29:58Z"
            }
            """;

    @Test
    void shouldRejectRequestWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message")
                        .value("A valid API key is required"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenApiKeyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/ingestion/measurements")
                        .header("X-API-Key", "invalid-api-key")
                        .contentType("application/json")
                        .content(VALID_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldAcceptRequestWhenApiKeyIsValid() throws Exception {
        when(ingestionService.ingest(any()))
                .thenReturn(new IngestionResult(
                        "ACCEPTED",
                        "corr-security-test"
                ));

        mockMvc.perform(post("/api/ingestion/measurements")
                        .header("X-API-Key", "test-api-key")
                        .contentType("application/json")
                        .content(VALID_REQUEST))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.correlationId")
                        .value("corr-security-test"));
    }
}
