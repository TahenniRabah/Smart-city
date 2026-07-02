package com.urbanhub.quality.application;

import com.urbanhub.quality.events.MeasurementReceivedEvent;
import com.urbanhub.quality.events.MeasurementRejectedEvent;
import com.urbanhub.quality.events.MeasurementValidatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class MeasurementQualityService {

    private static final String VALIDATED_EVENT_TYPE = "MeasurementValidated";
    private static final String REJECTED_EVENT_TYPE = "MeasurementRejected";
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
        String rejectionReason = rejectionReason(event);

        if (rejectionReason != null) {
            rejectedPublisher.publish(toRejectedEvent(event, rejectionReason));
            return;
        }


        validatedPublisher.publish(toValidatedEvent(event));
    }

    private String rejectionReason(MeasurementReceivedEvent event) {
        if (isBlank(event.zoneId())) {
            return "zoneId is required";
        }

        if (isBlank(event.stationId())) {
            return "stationId is required";
        }

        if (isBlank(event.indicator())) {
            return "indicator is required";
        }

        if (event.timestamp() == null) {
            return "timestamp is required";
        }

        if (event.value() < 0) {
            return "value must be positive or zero";
        }

        return null;
    }

    private MeasurementValidatedEvent toValidatedEvent(MeasurementReceivedEvent event) {
        return new MeasurementValidatedEvent(
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
    }

    private MeasurementRejectedEvent toRejectedEvent(
            MeasurementReceivedEvent event,
            String reason
    ) {
        return new MeasurementRejectedEvent(
                UUID.randomUUID().toString(),
                REJECTED_EVENT_TYPE,
                EVENT_VERSION,
                event.correlationId(),
                Instant.now(),
                SOURCE,
                event.zoneId(),
                event.stationId(),
                event.indicator(),
                event.value(),
                event.timestamp(),
                reason
        );
    }


    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}