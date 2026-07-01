package com.urbanhub.ingestion.application;


import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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
}

