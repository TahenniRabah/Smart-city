package com.urbanhub.ingestion.api;

import com.urbanhub.ingestion.application.MeasurementIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
class IngestionControllerValidationTest {

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
                  "timestamp": "2026-07-27T18:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.zoneId")
                        .value("zoneId is required"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenIndicatorIsUnsupported() throws Exception {
        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "UNKNOWN",
                  "value": 220.5,
                  "timestamp": "2026-07-27T18:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.indicator")
                        .value("indicator must be one of: NO2, PM10, PM25"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenValueIsNegative() throws Exception {
        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "value": -1,
                  "timestamp": "2026-07-27T18:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.value")
                        .value("value must be greater than or equal to 0"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenValueIsMissing() throws Exception {
        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "timestamp": "2026-07-27T18:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.value")
                        .value("value is required"));

        verifyNoInteractions(ingestionService);
    }

    @Test
    void shouldRejectRequestWhenTimestampIsMissing() throws Exception {
        String request = """
                {
                  "zoneId": "ZFE-1",
                  "stationId": "AIR-STATION-042",
                  "indicator": "NO2",
                  "value": 220.5
                }
                """;

        mockMvc.perform(post("/api/ingestion/measurements")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.timestamp")
                        .value("timestamp is required"));

        verifyNoInteractions(ingestionService);
    }
}