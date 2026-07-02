package com.urbanhub.quality.messaging;

import com.urbanhub.quality.application.MeasurementRejectedPublisher;
import com.urbanhub.quality.events.MeasurementRejectedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaMeasurementRejectedPublisher implements MeasurementRejectedPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topicName;

    public KafkaMeasurementRejectedPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${urbanhub.kafka.topics.measurements-rejected}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publish(MeasurementRejectedEvent event) {
        kafkaTemplate.send(topicName, event.zoneId(), event);
    }
}