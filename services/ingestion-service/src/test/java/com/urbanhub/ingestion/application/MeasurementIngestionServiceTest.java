package com.urbanhub.ingestion.application;


import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;



public class MeasurementIngestionServiceTest {

    private final MeasurementReceivedPublisher publisher = mock(MeasurementReceivedPublisher.class);
    private final CorrelationIdGenerator correlationIdGenerator = mock(CorrelationIdGenerator.class);

    private final MeasurementIngestionService service =
            new MeasurementIngestionService(publisher, correlationIdGenerator);

    @Test
    void shouldAcceptRawMeasurementAndPublishMeasurementReceivedEvent() {
        RawMeasurementCommand command = new RawMeasurementCommand(
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        when(correlationIdGenerator.generate()).thenReturn("corr-12345");

        IngestionResult result = service.ingest(command);

        assertEquals("ACCEPTED", result.status());
        assertEquals("corr-12345", result.correlationId());

        ArgumentCaptor<MeasurementReceivedEvent> eventCaptor =
                ArgumentCaptor.forClass(MeasurementReceivedEvent.class);

        verify(publisher).publish(eventCaptor.capture());

        MeasurementReceivedEvent event = eventCaptor.getValue();

        assertEquals("MeasurementReceived", event.eventType());
        assertEquals("1.0", event.eventVersion());
        assertEquals("corr-12345", event.correlationId());
        assertEquals("ingestion-service", event.source());
        assertEquals("ZFE-1", event.zoneId());
        assertEquals("AIR-STATION-042", event.stationId());
        assertEquals("NO2", event.indicator());
        assertEquals(220.5, event.value());
    }

    @Test
    void shouldRejectMeasurementWithoutZoneId() {
        RawMeasurementCommand command = new RawMeasurementCommand(
                "",
                "AIR-STATION-042",
                "NO2",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.ingest(command)
        );

        assertEquals("zoneId is required", exception.getMessage());
        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRejectUnsupportedIndicator() {
        RawMeasurementCommand command = new RawMeasurementCommand(
                "ZFE-1",
                "AIR-STATION-042",
                "UNKNOWN",
                220.5,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.ingest(command)
        );

        assertEquals(
                "indicator is not supported",
                exception.getMessage()
        );

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRejectNegativeValue() {
        RawMeasurementCommand command = new RawMeasurementCommand(
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                -1.0,
                Instant.parse("2026-05-06T14:29:58Z")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.ingest(command)
        );

        assertEquals(
                "value must be between 0 and 5000",
                exception.getMessage()
        );

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldRejectMissingTimestamp() {
        RawMeasurementCommand command = new RawMeasurementCommand(
                "ZFE-1",
                "AIR-STATION-042",
                "NO2",
                220.5,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.ingest(command)
        );

        assertEquals(
                "timestamp is required",
                exception.getMessage()
        );

        verifyNoInteractions(publisher);
    }
}

