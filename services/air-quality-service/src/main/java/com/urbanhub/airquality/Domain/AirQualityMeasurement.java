package com.urbanhub.airquality.Domain;

public record AirQualityMeasurement(
        String zoneId,
        String stationId,
        Pollutant pollutant,
        double value
) {
}
