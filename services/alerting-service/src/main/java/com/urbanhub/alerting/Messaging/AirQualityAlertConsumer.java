package com.urbanhub.alerting.Messaging;

import com.urbanhub.alerting.Application.AlertingService;
import com.urbanhub.alerting.Events.AirQualityAlertDetectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AirQualityAlertConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AirQualityAlertConsumer.class);

    private final AlertingService alertingService;

    public AirQualityAlertConsumer(AlertingService alertingService) {
        this.alertingService = alertingService;
    }

    @KafkaListener(
            topics = "${urbanhub.kafka.topics.air-quality-alert-detected}",
            containerFactory = "airQualityAlertKafkaListenerContainerFactory"
    )
    public void consume(AirQualityAlertDetectedEvent event) {

        log.info(
                "AirQualityAlertDetected consumed: eventId={}, correlationId={}, zoneId={}, stationId={}, pollutant={}, level={}",
                event.eventId(),
                event.correlationId(),
                event.zoneId(),
                event.stationId(),
                event.pollutant(),
                event.alertLevel()
        );

        alertingService.handle(event);
    }
}