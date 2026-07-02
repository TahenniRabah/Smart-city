package com.urbanhub.airquality.messaging;

import com.urbanhub.airquality.application.AirQualityAnalysisService;
import com.urbanhub.airquality.domain.AirQualityMeasurement;
import com.urbanhub.airquality.events.MeasurementValidatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MeasurementValidatedConsumerTest {

    private final AirQualityAnalysisService analysisService = mock(AirQualityAnalysisService.class);

    private final MeasurementValidatedConsumer consumer =
            new MeasurementValidatedConsumer(analysisService);

    @Test
    void shouldAnalyzeMeasurementWhenMeasurementValidatedEventIsConsumed() {
        MeasurementValidatedEvent event = new MeasurementValidatedEvent(
                "evt-validated-001",
                "MeasurementValidated",
                "1.0",
                "corr-12345",
                Instant.parse("2026-05-06T14:30:01Z"),
                "quality-service",
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        consumer.consume(event);

        ArgumentCaptor<AirQualityMeasurement> captor =
                ArgumentCaptor.forClass(AirQualityMeasurement.class);

        verify(analysisService).analyze(captor.capture());

        AirQualityMeasurement measurement = captor.getValue();

        assertEquals("ZFE-1", measurement.zoneId());
        assertEquals("AIR-STATION-042", measurement.stationId());
        assertEquals(220.5, measurement.value());
    }
}