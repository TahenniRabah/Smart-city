package com.urbanhub.airquality.Domain;

public record AirQualityMeasurement(
        String zoneId,
        Pollutant pollutant,
        double value
) {
}
