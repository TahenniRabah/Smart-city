package com.urbanhub.airquality.Application;


import com.urbanhub.airquality.Domain.AirQualityMeasurement;
import com.urbanhub.airquality.Domain.AlertLevel;
import org.springframework.stereotype.Service;

@Service
public class AirQualityAlertService {

    private static final double NO2_WARNING_THRESHOLD = 100;
    private static final double NO2_CRITICAL_THRESHOLD = 200;
    private static final double PM10_WARNING_THRESHOLD = 50;
    private static final double PM10_CRITICAL_THRESHOLD = 80;

    
    public AlertLevel calculateAlertLevel(AirQualityMeasurement measurement) {
        return switch (measurement.pollutant()){
            case NO2 -> calculateNo2Level(measurement.value());
            case PM10 -> calculatePM10Level(measurement.value());
            case PM25 -> null;
        };
    }

    private AlertLevel calculatePM10Level(double value) {

        if (value >= PM10_CRITICAL_THRESHOLD) {
            return AlertLevel.CRITICAL;
        }

        if (value >= PM10_WARNING_THRESHOLD) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.NORMAL;
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
