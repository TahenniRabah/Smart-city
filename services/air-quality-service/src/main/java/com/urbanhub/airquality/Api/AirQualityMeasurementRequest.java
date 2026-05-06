package com.urbanhub.airquality.Api;

import com.urbanhub.airquality.Domain.Pollutant;

public record AirQualityMeasurementRequest(
        String zoneId,
        String stationId,
        Pollutant pollutant,
        double value
) {
}