package com.urbanhub.quality.messaging;

import com.urbanhub.quality.application.MeasurementValidatedPublisher;
import com.urbanhub.quality.events.MeasurementValidatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMeasurementValidatedPublisher implements MeasurementValidatedPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicName;

    public KafkaMeasurementValidatedPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${urbanhub.kafka.topics.measurements-validated}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publish(MeasurementValidatedEvent event) {
        kafkaTemplate.send(topicName, event.zoneId(), event);
    }
}