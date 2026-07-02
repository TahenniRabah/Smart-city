package com.urbanhub.quality.events;


import java.time.Instant;

public record MeasurementReceivedEvent(
        String eventId,
        String eventType,
        String eventVersion,
        String correlationId,
        Instant occurredAt,
        String source,
        String zoneId,
        String stationId,
        String indicator,
        double value,
        Instant timestamp
) {
}

