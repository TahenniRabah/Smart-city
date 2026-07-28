package com.urbanhub.ingestion.api;

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

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class IngestionControllerValidationTest {

    private static final String ENDPOINT =
            "/api/ingestion/measurements";

    private static final String API_KEY_HEADER =
            "X-API-Key";

    private static final String VALID_API_KEY =
            "test-api-key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeasurementIngestionService ingestionService;

    @Test
    void shouldRejectRequestWhenZoneIdIsBlank() throws Exception {
        String request = """
                {
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "value": 220.5,
                  "timestamp": "2026-07-27T08:00:00Z"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.zoneId")
                        .value("zoneId is required"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenIndicatorIsUnsupported()
            throws Exception {

        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "UNKNOWN",
                  "value": 220.5,
                  "timestamp": "2026-07-27T08:00:00Z"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.indicator")
                        .value(
                                "indicator must be one of: NO2, PM10, PM25"
                        ));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenValueIsNegative()
            throws Exception {

        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "value": -1,
                  "timestamp": "2026-07-27T08:00:00Z"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.value")
                        .value(
                                "value must be greater than or equal to 0"
                        ));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenValueIsMissing()
            throws Exception {

        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "timestamp": "2026-07-27T08:00:00Z"
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.value")
                        .value("value is required"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenTimestampIsMissing()
            throws Exception {

        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "value": 220.5
                }
                """;

        mockMvc.perform(post(ENDPOINT)
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.timestamp")
                        .value("timestamp is required"));

        verifyNoInteractions(ingestionService);
    }
}