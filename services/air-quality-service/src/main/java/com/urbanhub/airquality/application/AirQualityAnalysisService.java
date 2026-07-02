
package com.urbanhub.airquality.application;

import com.urbanhub.airquality.domain.AirQualityMeasurement;
import com.urbanhub.airquality.domain.AlertLevel;
import com.urbanhub.airquality.domain.Pollutant;
import com.urbanhub.airquality.events.AirQualityAlertDetectedEvent;
import com.urbanhub.airquality.messaging.AirQualityAlertProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AirQualityAnalysisService {

    private static final String EVENT_TYPE = "AirQualityAlertDetected";
    private static final String EVENT_VERSION = "1.0";
    private static final String SOURCE = "air-quality-service";
    private static final String UNIT = "µg/m3";

    private final AirQualityAlertService alertService;
    private final AirQualityAlertProducer producer;

    public AirQualityAnalysisService(
            AirQualityAlertService alertService,
            AirQualityAlertProducer producer
    ) {
        this.alertService = alertService;
        this.producer = producer;
    }

    public AlertLevel analyze(AirQualityMeasurement measurement) {
        AlertLevel alertLevel = alertService.calculateAlertLevel(measurement);

        if (alertLevel == AlertLevel.WARNING || alertLevel == AlertLevel.CRITICAL) {
            producer.publish(toEvent(measurement, alertLevel));
        }

        return alertLevel;
    }

    private AirQualityAlertDetectedEvent toEvent(
            AirQualityMeasurement measurement,
            AlertLevel alertLevel
    ) {
        return new AirQualityAlertDetectedEvent(
                UUID.randomUUID().toString(),
                EVENT_TYPE,
                EVENT_VERSION,
                Instant.now(),
                UUID.randomUUID().toString(),
                SOURCE,
                measurement.zoneId(),
                measurement.stationId(),
                measurement.pollutant().name(),
                measurement.value(),
                UNIT,
                alertLevel.name(),
                resolveThreshold(measurement.pollutant(), alertLevel)
        );
    }

    private double resolveThreshold(Pollutant pollutant, AlertLevel alertLevel) {
        if (pollutant == Pollutant.NO2 && alertLevel == AlertLevel.CRITICAL) {
            return 200;
        }
        if (pollutant == Pollutant.NO2 && alertLevel == AlertLevel.WARNING) {
            return 100;
        }
        if (pollutant == Pollutant.PM10 && alertLevel == AlertLevel.CRITICAL) {
            return 80;
        }
        if (pollutant == Pollutant.PM10 && alertLevel == AlertLevel.WARNING) {
            return 50;
        }

        return 0;
    }
}
