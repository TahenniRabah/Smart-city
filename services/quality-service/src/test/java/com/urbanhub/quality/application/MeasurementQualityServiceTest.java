package com.urbanhub.quality.application;

import com.urbanhub.quality.events.MeasurementReceivedEvent;
import com.urbanhub.quality.events.MeasurementValidatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MeasurementQualityServiceTest {

    private final MeasurementValidatedPublisher validatedPublisher =
            mock(MeasurementValidatedPublisher.class);

    private final MeasurementRejectedPublisher rejectedPublisher =
            mock(MeasurementRejectedPublisher.class);

    private final MeasurementQualityService service =
            new MeasurementQualityService(validatedPublisher, rejectedPublisher);

    @Test
    void shouldPublishMeasurementValidatedWhenMeasurementIsValid() {
        MeasurementReceivedEvent receivedEvent = validMeasurementReceived();

        service.checkQuality(receivedEvent);

        ArgumentCaptor<MeasurementValidatedEvent> captor =
                ArgumentCaptor.forClass(MeasurementValidatedEvent.class);

        verify(validatedPublisher).publish(captor.capture());
        verifyNoInteractions(rejectedPublisher);

        MeasurementValidatedEvent validatedEvent = captor.getValue();

        assertEquals("MeasurementValidated", validatedEvent.eventType());
        assertEquals("1.0", validatedEvent.eventVersion());
        assertEquals("corr-12345", validatedEvent.correlationId());
        assertEquals("quality-service", validatedEvent.source());
        assertEquals("ZFE-1", validatedEvent.zoneId());
        assertEquals("AIR-STATION-042", validatedEvent.stationId());
        assertEquals("NO2", validatedEvent.indicator());
        assertEquals(220.5, validatedEvent.value());
    }

    private MeasurementReceivedEvent validMeasurementReceived() {
        return new MeasurementReceivedEvent(
                "evt-001",
                "MeasurementReceived",
                "1.0",
                "corr-12345",
                Instant.parse("2026-05-06T14:30:00Z"),
                "ingestion-service",
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );
    }

    @Test
    void shouldPublishMeasurementRejectedWhenZoneIdIsMissing() {
        MeasurementReceivedEvent receivedEvent = new MeasurementReceivedEvent(
                "evt-002",
                "MeasurementReceived",
                "1.0",
                "corr-12345",
                Instant.parse("2026-05-06T14:30:00Z"),
                "ingestion-service",
                "",
                "AIR-STATION-042",
                "NO2",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        service.checkQuality(receivedEvent);

        verifyNoInteractions(validatedPublisher);
        verify(rejectedPublisher).publish(argThat(rejectedEvent ->
                rejectedEvent.eventType().equals("MeasurementRejected")
                        && rejectedEvent.correlationId().equals("corr-12345")
                        && rejectedEvent.reason().equals("zoneId is required")
        ));
    }
}