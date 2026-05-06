package com.urbanhub.airquality.events;

import java.time.Instant;

public record AirQualityAlertDetectedEvent(
        String eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,
        String correlationId,
        String source,
        String zoneId,
        String stationId,
        String pollutant,
        double measuredValue,
        String unit,
        String alertLevel,
        double threshold
) {
}
