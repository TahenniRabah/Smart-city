package com.urbanhub.airquality.Application;

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
                new AirQualityMeasurement("ZFE-1", Pollutant.NO2, 220);

        AlertLevel result = service.calculateAlertLevel(measurement);

        assertEquals(AlertLevel.CRITICAL, result);
    }


}