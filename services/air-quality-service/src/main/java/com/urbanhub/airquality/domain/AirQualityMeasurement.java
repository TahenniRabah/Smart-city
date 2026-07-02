package com.urbanhub.airquality.domain;

public record AirQualityMeasurement(
        String zoneId,
        String stationId,
        Pollutant pollutant,
        double value
) {
}
