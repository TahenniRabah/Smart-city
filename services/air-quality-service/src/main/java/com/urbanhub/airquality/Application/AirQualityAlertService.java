package com.urbanhub.airquality.Application;


import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import com.urbanhub.airquality.Domain.Pollutant;

public class AirQualityAlertService {

    private static final double NO2_WARNING_THRESHOLD = 100;
    private static final double NO2_CRITICAL_THRESHOLD = 200;
    private static final double PM10_WARNING_THRESHOLD = 50;
    private static final double PM10_CRITICAL_THRESHOLD = 80;

    
    public AlertLevel calculateAlertLevel(AirQualityMeasurement measurement) {
        
        if (measurement.pollutant() == Pollutant.NO2) {
            return calculateNo2Level(measurement.value());
        }
        if (measurement.pollutant() == Pollutant.PM10) {
            return calculatePM10Level(measurement.value());
        }

        return null;
    }

    private AlertLevel calculatePM10Level(double value) {

        if (value >= PM10_CRITICAL_THRESHOLD) {
            return AlertLevel.CRITICAL;
        }

        if (value >= PM10_WARNING_THRESHOLD) {
            return AlertLevel.WARNING;
        }
        return null;
    }


    private AlertLevel calculateNo2Level(double value) {

        if (value >= NO2_CRITICAL_THRESHOLD) {
            return AlertLevel.CRITICAL;
        }

        if (value >= NO2_WARNING_THRESHOLD) {
            return AlertLevel.WARNING;
        }

        return AlertLevel.NORMAL;
    }

}
