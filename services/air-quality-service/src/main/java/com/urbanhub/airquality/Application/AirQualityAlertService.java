package com.urbanhub.airquality.Application;


import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import com.urbanhub.airquality.Domain.Pollutant;

public class AirQualityAlertService {

    public AlertLevel calculateAlertLevel(AirQualityMeasurement measurement) {

        if (measurement.pollutant() == Pollutant.NO2 && measurement.value() >= 200) {
            return AlertLevel.CRITICAL;
        }


        if (measurement.pollutant() == Pollutant.NO2 && measurement.value() >= 100) {
            return AlertLevel.WARNING;
        }

        return AlertLevel.NORMAL;
    }
}
