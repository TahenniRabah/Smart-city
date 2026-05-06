package com.urbanhub.airquality.messaging;

import com.urbanhub.airquality.events.AirQualityAlertDetectedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AirQualityAlertProducer {

    private final KafkaTemplate<String, AirQualityAlertDetectedEvent> kafkaTemplate;
    private final String topicName;

    public AirQualityAlertProducer(
            KafkaTemplate<String, AirQualityAlertDetectedEvent> kafkaTemplate,
            @Value("${urbanhub.kafka.topics.air-quality-alert-detected}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publish(AirQualityAlertDetectedEvent event) {
        kafkaTemplate.send(topicName, event.zoneId(), event);
    }
}