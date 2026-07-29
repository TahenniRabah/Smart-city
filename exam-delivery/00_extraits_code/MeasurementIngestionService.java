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
        if (command == null) {
            throw new IllegalArgumentException("measurement command is required");
        }

        if (isBlank(command.zoneId())) {
            throw new IllegalArgumentException("zoneId is required");
        }

        if (isBlank(command.stationId())) {
            throw new IllegalArgumentException("stationId is required");
        }

        if (isBlank(command.indicator())) {
            throw new IllegalArgumentException("indicator is required");
        }

        if (!isSupportedIndicator(command.indicator())) {
            throw new IllegalArgumentException("indicator is not supported");
        }

        if (command.value() < 0 || command.value() > 5000) {
            throw new IllegalArgumentException(
                    "value must be between 0 and 5000"
            );
        }

        if (command.timestamp() == null) {
            throw new IllegalArgumentException("timestamp is required");
        }

        if (command.timestamp().isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "timestamp must not be in the future"
            );
        }
    }

    private boolean isSupportedIndicator(String indicator) {
        return switch (indicator) {
            case "NO2", "PM10", "PM25" -> true;
            default -> false;
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
