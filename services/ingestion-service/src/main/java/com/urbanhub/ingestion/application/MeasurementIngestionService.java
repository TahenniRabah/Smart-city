package com.urbanhub.ingestion.application;


import com.urbanhub.ingestion.events.MeasurementReceivedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MeasurementIngestionService {

    private static final String EVENT_TYPE = "MeasurementReceived";
    private static final String EVENT_VERSION = "1.0";
    private static final String SOURCE = "ingestion-service";
    private static final String ACCEPTED = "ACCEPTED";

    private final MeasurementReceivedPublisher publisher;
    private final CorrelationIdGenerator correlationIdGenerator;

    public MeasurementIngestionService(
            MeasurementReceivedPublisher publisher,
            CorrelationIdGenerator correlationIdGenerator
    ) {
        this.publisher = publisher;
        this.correlationIdGenerator = correlationIdGenerator;
    }

    public IngestionResult ingest(RawMeasurementCommand command) {
        validate(command);
        String correlationId = correlationIdGenerator.generate();

        MeasurementReceivedEvent event = new MeasurementReceivedEvent(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                EVENT_VERSION,
                correlationId,
                Instant.now(),
                SOURCE,
                command.zoneId(),
                command.stationId(),
                command.indicator(),
                command.value(),
                command.timestamp()
        );

        publisher.publish(event);

        return new IngestionResult(ACCEPTED, correlationId);
    }

    private void validate(RawMeasurementCommand command) {
        if (command.zoneId() == null || command.zoneId().isBlank()) {
            throw new IllegalArgumentException("zoneId is required");
        }
    }
}
