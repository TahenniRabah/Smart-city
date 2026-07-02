package com.urbanhub.airquality.application;

import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import com.urbanhub.airquality.Domain.Pollutant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AirQualityAlertServiceTest {

    private final AirQualityAlertService service = new AirQualityAlertService();



    @Test
    void shouldReturnCriticalWhenNo2IsAboveCriticalThreshold() {
        AirQualityMeasurement measurement =
                new AirQualityMeasurement("ZFE-1", "AIR-STATION-042",Pollutant.NO2, 220);

        AlertLevel result = service.calculateAlertLevel(measurement);

        assertEquals(AlertLevel.CRITICAL, result);
    }


    @Test
    void shouldReturnWarningWhenNo2IsBetweenWarningAndCriticalThreshold() {
        AirQualityMeasurement measurement =
                new AirQualityMeasurement("ZFE-1", "AIR-STATION-042", Pollutant.NO2, 150);

        AlertLevel result = service.calculateAlertLevel(measurement);

        assertEquals(AlertLevel.WARNING, result);
    }

    @Test
    void shouldReturnNormalWhenNo2IsBelowWarningThreshold() {
        AirQualityMeasurement measurement =
                new AirQualityMeasurement("ZFE-1", "AIR-STATION-042", Pollutant.NO2, 80);

        AlertLevel result = service.calculateAlertLevel(measurement);

        assertEquals(AlertLevel.NORMAL, result);
    }

    @Test
    void shouldReturnCriticalWhenPm10IsAboveCriticalThreshold() {
        AirQualityMeasurement measurement =
                new AirQualityMeasurement("ZFE-2", "AIR-STATION-042",Pollutant.PM10, 90);

        AlertLevel result = service.calculateAlertLevel(measurement);

        assertEquals(AlertLevel.CRITICAL, result);
    }

}