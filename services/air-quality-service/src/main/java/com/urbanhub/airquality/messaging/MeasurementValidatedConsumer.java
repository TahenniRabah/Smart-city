package com.urbanhub.airquality.messaging;

import com.urbanhub.airquality.application.AirQualityAnalysisService;
import com.urbanhub.airquality.domain.AirQualityMeasurement;
import com.urbanhub.airquality.domain.Pollutant;
import com.urbanhub.airquality.events.MeasurementValidatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeasurementValidatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementValidatedConsumer.class);

    private final AirQualityAnalysisService analysisService;

    public MeasurementValidatedConsumer(AirQualityAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @KafkaListener(
            topics = "${urbanhub.kafka.topics.measurements-validated}",
            containerFactory = "measurementValidatedKafkaListenerContainerFactory"
    )
    public void consume(MeasurementValidatedEvent event) {
        log.info(
                "Measurement validated consumed: correlationId={}, zoneId={}, stationId={}, indicator={}, value={}",
                event.correlationId(),
                event.zoneId(),
                event.stationId(),
                event.indicator(),
                event.value()
        );

        AirQualityMeasurement measurement = new AirQualityMeasurement(
                event.zoneId(),
                event.stationId(),
                Pollutant.valueOf(event.indicator()),
                event.value()
        );

        analysisService.analyze(measurement);
    }
}