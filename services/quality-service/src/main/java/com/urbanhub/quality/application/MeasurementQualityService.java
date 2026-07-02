package com.urbanhub.quality.application;

import com.urbanhub.quality.events.MeasurementReceivedEvent;
import com.urbanhub.quality.events.MeasurementValidatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MeasurementQualityService {

    private static final String VALIDATED_EVENT_TYPE = "MeasurementValidated";
    private static final String EVENT_VERSION = "1.0";
    private static final String SOURCE = "quality-service";

    private final MeasurementValidatedPublisher validatedPublisher;
    private final MeasurementRejectedPublisher rejectedPublisher;

    public MeasurementQualityService(
            MeasurementValidatedPublisher validatedPublisher,
            MeasurementRejectedPublisher rejectedPublisher
    ) {
        this.validatedPublisher = validatedPublisher;
        this.rejectedPublisher = rejectedPublisher;
    }

    public void checkQuality(MeasurementReceivedEvent event) {
        MeasurementValidatedEvent validatedEvent = new MeasurementValidatedEvent(
                UUID.randomUUID().toString(),
                VALIDATED_EVENT_TYPE,
                EVENT_VERSION,
                event.correlationId(),
                Instant.now(),
                SOURCE,
                event.zoneId(),
                event.stationId(),
                event.indicator(),
                event.value(),
                event.timestamp()
        );

        validatedPublisher.publish(validatedEvent);
    }
}