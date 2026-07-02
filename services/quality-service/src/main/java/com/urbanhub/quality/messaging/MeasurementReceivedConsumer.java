package com.urbanhub.quality.messaging;

import com.urbanhub.quality.application.MeasurementQualityService;
import com.urbanhub.quality.events.MeasurementReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeasurementReceivedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementReceivedConsumer.class);

    private final MeasurementQualityService qualityService;

    public MeasurementReceivedConsumer(MeasurementQualityService qualityService) {
        this.qualityService = qualityService;
    }

    @KafkaListener(
            topics = "${urbanhub.kafka.topics.measurements-received}",
            containerFactory = "measurementReceivedKafkaListenerContainerFactory"
    )
    public void consume(MeasurementReceivedEvent event) {
        log.info(
                "Measurement received for quality check: correlationId={}, zoneId={}, stationId={}, indicator={}",
                event.correlationId(),
                event.zoneId(),
                event.stationId(),
                event.indicator()
        );

        qualityService.checkQuality(event);
    }
}