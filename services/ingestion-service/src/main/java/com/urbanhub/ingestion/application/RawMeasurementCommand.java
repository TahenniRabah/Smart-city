package com.urbanhub.ingestion.application;


import java.time.Instant;

public record RawMeasurementCommand(
        String zoneId,
        String stationId,
        String indicator,
        double value,
        Instant timestamp
) {
}
